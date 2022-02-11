package cc.w0rm.douban.service;

import cc.w0rm.douban.model.DoubanAnalyseModel;
import cc.w0rm.douban.model.DoubanListResponseDTO;
import cc.w0rm.douban.model.Pipe;
import cc.w0rm.douban.model.ProducerAction;
import cc.w0rm.douban.util.CollUtil;
import cc.w0rm.douban.util.FileUtil;
import cc.w0rm.douban.util.JsonUtil;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xuyang
 * @date 2022/2/9
 */
public class TextProcessor {

    public static final Set<String> CUSTOMER_NAS = Sets.newHashSet(Nature.FACILITY.getDesc(),
            Nature.HOUSE.getDesc(), Nature.AREA.getDesc(), Nature.STREET.getDesc(), Nature.SUBWAY.getDesc());
    public static final Set<String> DEFAULT_NAS = Sets.newHashSet(Nature.M.getDesc(),
            Nature.NS.getDesc(), Nature.NR.getDesc());

    private static final int MIN_M_NATURE_LENGTH = 3;

    static class TextProducerAction implements ProducerAction {

        private volatile boolean status = false;
        private Pipe pipe;

        public TextProducerAction(Pipe pipe) {
            this.pipe = pipe;
        }

        @Override
        public boolean finished() {
            return pipe.finished() && status;
        }

        @Override
        public void setFinish() {
            status = true;
        }
    }

    public static Pipe<DoubanAnalyseModel> process(Pipe<DoubanAnalyseModel> pipeWebResource) {
        ProducerAction action = new TextProducerAction(pipeWebResource);
        Pipe<DoubanAnalyseModel> pipe = new Pipe<>(action);

        new Thread(() -> {
            while (true) {
                DoubanAnalyseModel task = pipeWebResource.getTask();
                if (Objects.isNull(task)) {
                    break;
                }
                DoubanListResponseDTO.ItemDTO item = task.getItem();
                String mainContent = getMainContent(task.getHtml());
                Map<String, Object> contentMap = getContentMap(mainContent);
                String context = item.getTitle() + "\n" + contentMap.getOrDefault("text", StringUtils.EMPTY);
                Result result = DoubanAnalysis.parse(context);
                List<Term> terms = result.getTerms();
                Map<String, String> tagMap = new HashMap<>(terms.size() / 2);

                String predictPrice = StringUtils.EMPTY;

                for (Term t : terms) {
                    String natureStr = t.getNatureStr();
                    if (!CUSTOMER_NAS.contains(natureStr) && !DEFAULT_NAS.contains(natureStr)) {
                        continue;
                    }
                    if (DEFAULT_NAS.contains(natureStr) && t.getName().length() < MIN_M_NATURE_LENGTH) {
                        continue;
                    }
                    if (Nature.M.getDesc().equals(natureStr)) {
                        predictPrice = predictPriceFromName(t.getName(), predictPrice);
                    }

                    tagMap.put(t.getName(), natureStr);
                }

                task.setHtmlContent(mainContent);
                task.setContentMap(contentMap);
                task.setResult(result);
                task.setTagMap(tagMap);
                task.setPrice(predictPrice);

                pipe.putTask(task);
            }
            action.setFinish();
            System.out.println("=== TextProcessor 文本解析完成===");
        }).start();

        return pipe;
    }


    private static final Pattern PATTERN_PRICE = Pattern.compile("[1-9][0-9]{2}0[0]?");
    private static final String PRICE_SPLIT = ".";

    private static String predictPriceFromName(String name, String prePrice) {
        if (StringUtils.isBlank(name)) {
            return prePrice;
        }
        try {
            Matcher matcher = PATTERN_PRICE.matcher(name);
            if (!matcher.find()) {
                return prePrice;
            }
            String priceStr = matcher.group();
            int price = Integer.parseInt(priceStr);
            if (StringUtils.isBlank(prePrice)) {
                return price + PRICE_SPLIT + price;
            }

            String[] split = prePrice.split("[.]");

            int min = Integer.parseInt(split[0]);
            int max = Integer.parseInt(split[1]);
            if (price > max) {
                return min + PRICE_SPLIT + price;
            } else if (price < min) {
                return price + PRICE_SPLIT + max;
            }

            return prePrice;
        } catch (Exception e) {
            e.printStackTrace();
            return prePrice;
        }
    }

    private static Map<String, Object> getContentMap(String mainContent) {
        if (StringUtils.isBlank(mainContent)) {
            return Collections.emptyMap();
        }

        try {
            return JsonUtil.toObject(mainContent, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }


    private static final Pattern PATTERN = Pattern.compile("<script type=\"application/ld[+]json\">([\\s\\S]+?)</script>");
    private static final Pattern PATTERN_SS = Pattern.compile("\\s+");

    private static String getMainContent(String html) {
        if (StringUtils.isBlank(html)) {
            return StringUtils.EMPTY;
        }

        Matcher matcher = PATTERN.matcher(html);
        if (!matcher.find()) {
            return StringUtils.EMPTY;
        }

        String group = matcher.group(1);
        matcher = PATTERN_SS.matcher(group);
        if (!matcher.find()) {
            return group;
        }

        return matcher.replaceAll("");
    }

    @Getter
    @AllArgsConstructor
    enum Nature {
        FACILITY("facility", 13),
        HOUSE("house", 14),
        STREET("street", 15),
        SUBWAY("subway", 16),
        AREA("area", 17),
        M("m", 1),
        NR("nr", 2),
        NS("ns", 3);
        private String desc;
        private int code;

        private static final Map<String, Nature> INNER_MAP = Stream.of(values())
                .collect(Collectors.toMap(Nature::getDesc, Function.identity()));

        public static Nature getEnumByDesc(String desc) {
            return INNER_MAP.get(desc);
        }

    }

    static class DoubanAnalysis {

        static {
            loadDic();
        }

        private static void loadDic() {
            List<String> words = null;

            words = FileUtil.readResources("library/facilities.txt");
            loadDic(words, Nature.FACILITY.getDesc(), 99999);

            words = FileUtil.readResources("library/house.txt");
            loadDic(words, Nature.HOUSE.getDesc(), 99999);

            words = FileUtil.readResources("library/subways.txt");
            loadDic(words, Nature.SUBWAY.getDesc(), 99999);

            words = FileUtil.readResources("library/streets.txt");
            loadDic(words, Nature.STREET.getDesc(), 99999);

            words = FileUtil.readResources("library/area.txt");
            loadDic(words, Nature.AREA.getDesc(), 99999);

        }


        private static void loadDic(List<String> sList, String nature, int feq) {
            if (CollUtil.isEmpty(sList)) {
                return;
            }
            for (String s : sList) {
                if (StringUtils.isBlank(s)) {
                    continue;
                }
                DicLibrary.insert(DicLibrary.DEFAULT, s, nature, feq);
            }
        }

        public static Result parse(String str) {
            return (new DicAnalysis()).parseStr(str);
        }

    }

}
