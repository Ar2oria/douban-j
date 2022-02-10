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

    public static void run(String place, List<String> tagList, int day, String cookie) {
        preProcessTagList(tagList);
        DBService service = new DBService();
        Pipe<DoubanAnalyseModel> pipeWebResource = Downloader.search(place, cookie, service.getFilter());
        Pipe<DoubanAnalyseModel> pipeText = TextProcessor.process(pipeWebResource);
        service.save(pipeText);
        search(service, pipeText, place, tagList, day);
    }

    private static void preProcessTagList(List<String> tagList) {
        if (CollUtil.isEmpty(tagList)) {
            return;
        }
        if (tagList.contains("无中介")) {
            tagList.add("无中介费");
            tagList.add("免中介费");
            tagList.add("没中介费");
            tagList.add("没有中介费");

            tagList.add("无服务费");
            tagList.add("免服务费");
            tagList.add("没服务费");
            tagList.add("没有服务费");

            tagList.add("房东直租");
        }
    }

    private static void search(DBService service, Pipe<DoubanAnalyseModel> pipe, String place, List<String> tagList, int day) {
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
                if (CollUtil.isEmpty(doubanIdList)) {
                    continue;
                }

                Map<Long, List<Tag>> doubanTagMap = service.selectTagByDoubanIdList(doubanIdList).stream()
                        .collect(Collectors.groupingBy(Tag::getDoubanId));

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

                                Set<String> tagSet = new HashSet<>(tagList);
                                tagSet.retainAll(tNameSet);

                                return tagSet.size() > 0;
                            }).collect(Collectors.toList());
                    if (CollUtil.isEmpty(doubanList)) {
                        continue;
                    }
                }

                Set<String> tagSet = new HashSet<>(tagList);
                Collections.sort(doubanList, (a, b) -> {
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

                    System.out.println("| " + douban.getId() + " | " + DateUtil.format(douban.getPubTime()) + " | " + douban.getTitle() + " | " + douban.getUrl() + " | " + tagStr + " |");
                }
                filterSet.addAll(doubanIdList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
