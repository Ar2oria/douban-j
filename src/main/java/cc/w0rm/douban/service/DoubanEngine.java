package cc.w0rm.douban.service;

import cc.w0rm.douban.db.Douban;
import cc.w0rm.douban.db.Tag;
import cc.w0rm.douban.model.DoubanAnalyseModel;
import cc.w0rm.douban.model.Pipe;
import cc.w0rm.douban.util.CollUtil;
import cc.w0rm.douban.util.DateUtil;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xuyang
 * @date 2022/2/9
 */
public class DoubanEngine {

    public static void run(String place, List<String> tagList, boolean tagSelect, boolean systemTag, int day, String cookie) {
        DBService service = new DBService();
        Pipe<DoubanAnalyseModel> pipeWebResource = Downloader.search(place, cookie, day, service.getFilter());
        Pipe<DoubanAnalyseModel> pipeText = TextProcessor.process(pipeWebResource);
        service.save(pipeText);
        search(service, pipeText, place, tagList, tagSelect, systemTag, day);
    }

    private static List<String> getSystemTagList() {
        List<String> tagList = new ArrayList<>();
        tagList.add("无中介费");
        tagList.add("免中介费");
        tagList.add("没中介费");
        tagList.add("没有中介费");

        tagList.add("无服务费");
        tagList.add("免服务费");
        tagList.add("没服务费");
        tagList.add("没有服务费");

        tagList.add("房东直租");
        return tagList;
    }

    private static void search(DBService service, Pipe<DoubanAnalyseModel> pipe, String place, List<String> tagList, boolean tagSelect, boolean systemTag, int day) {
        System.out.println("===================================================");
        long now = Instant.now().toEpochMilli();
        Set<Long> filterSet = new HashSet<>();
        while (!pipe.finished()) {
            try {
                Thread.sleep(10000);
                System.out.println("正在搜索房源中...");
                List<Douban> doubanList = service.selectDoubanByPlace(place);
                Collection<Douban> values = doubanList.stream()
                        .filter(douban -> !filterSet.contains(douban.getId()))
                        .filter(douban -> DateUtil.msToDays(now - douban.getPubTime().getTime()) < day)
                        .collect(Collectors.toMap(Douban::getWebId, Function.identity(), (a, b) -> a))
                        .values();
                doubanList = new ArrayList<>(values);
                if (CollUtil.isEmpty(doubanList)) {
                    continue;
                }

                List<Long> doubanIdList = doubanList.stream()
                        .map(Douban::getId)
                        .collect(Collectors.toList());
                Map<Long, List<Tag>> doubanTagMap = service.selectTagByDoubanIdList(doubanIdList).stream()
                        .collect(Collectors.groupingBy(Tag::getDoubanId));

                filterSet.addAll(doubanIdList);

                if (systemTag) {
                    Set<String> systemTagSet = new HashSet<>(getSystemTagList());
                    if (!CollUtil.isEmpty(systemTagSet)) {
                        doubanList = doubanList.stream()
                                .filter(douban -> {
                                    List<Tag> tags = doubanTagMap.get(douban.getId());
                                    if (CollUtil.isEmpty(tags)) {
                                        return false;
                                    }
                                    Set<String> tNameSet = tags.stream()
                                            .map(Tag::getTagText)
                                            .filter(systemTagSet::contains)
                                            .collect(Collectors.toSet());

                                    return !tNameSet.isEmpty();
                                }).collect(Collectors.toList());
                    }
                }

                Set<String> useTagSet = new HashSet<>(tagList);
                if (!CollUtil.isEmpty(tagList)) {
                    doubanList = doubanList.stream()
                            .filter(douban -> {
                                List<Tag> tags = doubanTagMap.get(douban.getId());
                                if (CollUtil.isEmpty(tags)) {
                                    return false;
                                }
                                Set<String> tNameSet = tags.stream()
                                        .map(Tag::getTagText)
                                        .collect(Collectors.toSet());

                                tNameSet.retainAll(useTagSet);
                                return tagSelect ? tNameSet.size() == useTagSet.size()
                                        : !tNameSet.isEmpty();
                            }).collect(Collectors.toList());
                    if (CollUtil.isEmpty(doubanList)) {
                        continue;
                    }
                }

                Set<String> tagSet = new HashSet<>(tagList);
                Collections.sort(doubanList, (a, b) -> {
                    String aPredictPrice = a.getPredictPrice();
                    String bPredictPrice = b.getPredictPrice();
                    if (!aPredictPrice.equals(bPredictPrice)) {
                        return bPredictPrice.compareTo(aPredictPrice);
                    }

                    List<Tag> aTagList = doubanTagMap.getOrDefault(a.getId(), Collections.emptyList());
                    List<Tag> bTagList = doubanTagMap.getOrDefault(b.getId(), Collections.emptyList());

                    int aCustomerTagNum = 0;
                    int bCustomerTagNum = 0;

                    int aSearchTagNum = 0;
                    int bSearchTagNum = 0;

                    for (Tag t : aTagList) {
                        TextProcessor.Nature nature = TextProcessor.Nature.getEnumByDesc(t.getNature());
                        if (nature.getCode() >= 10) {
                            aCustomerTagNum++;
                        }
                        if (tagSet.contains(t.getTagText())) {
                            aSearchTagNum++;
                        }
                    }

                    for (Tag t : bTagList) {
                        TextProcessor.Nature nature = TextProcessor.Nature.getEnumByDesc(t.getNature());
                        if (nature.getCode() >= 10) {
                            bCustomerTagNum++;
                        }
                        if (tagSet.contains(t.getTagText())) {
                            bSearchTagNum++;
                        }
                    }

                    if (aSearchTagNum != bSearchTagNum) {
                        return bSearchTagNum - aSearchTagNum;
                    }

                    if (aCustomerTagNum == bCustomerTagNum) {
                        return bTagList.size() - aTagList.size();
                    } else {
                        return bCustomerTagNum - aCustomerTagNum;
                    }
                });

                for (Douban douban : doubanList) {
                    String tagStr = "";
                    List<Tag> tags = doubanTagMap.get(douban.getId());
                    if (!CollUtil.isEmpty(tags)) {
                        Collections.sort(tags, (a, b) -> {
                            TextProcessor.Nature aEnum = TextProcessor.Nature.getEnumByDesc(a.getNature());
                            TextProcessor.Nature bEnum = TextProcessor.Nature.getEnumByDesc(b.getNature());
                            if (aEnum.getCode() == bEnum.getCode()) {
                                return b.getTagText().length() - a.getTagText().length();
                            }
                            return bEnum.getCode() - aEnum.getCode();
                        });
                        if (tags.size() > 30) {
                            tags = tags.subList(0, 30);
                        }
                        tagStr = tags.stream()
                                .map(Tag::getTagText)
                                .collect(Collectors.joining(","));
                    }

                    System.out.println("| " + douban.getId() + " | " + DateUtil.format(douban.getPubTime()) + " | " + douban.getAuthor() + " ｜ " + douban.getTitle() + " | " + douban.getPredictPrice() + " | " + douban.getUrl() + " | " + tagStr + " |");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(2000L);
            System.out.println("=== 数据搜索完成 ===");
        } catch (Exception e) {
            //do nothing
        }

    }
}
