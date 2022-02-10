package cc.w0rm.douban.service;

import cc.w0rm.douban.model.DoubanAnalyseModel;
import cc.w0rm.douban.model.DoubanListResponseDTO;
import cc.w0rm.douban.model.Pipe;
import cc.w0rm.douban.model.ProducerAction;
import cc.w0rm.douban.util.CollUtil;
import cc.w0rm.douban.util.FileUtil;
import cc.w0rm.douban.util.JsonUtil;
import com.google.common.collect.Sets;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xuyang
 * @date 2022/2/9
 */
public class TextProcessor {

    private static final Set<String> CUSTOMER_NAS = Sets.newHashSet("facility", "house", "subway", "street", "area");
    private static final Set<String> DEFAULT_NAS = Sets.newHashSet("m", "nr", "ns");

    private static final int MIN_M_NATURE_LENGTH = 2;

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

                for (Term t : terms) {
                    String natureStr = t.getNatureStr();
                    if (!CUSTOMER_NAS.contains(natureStr) && !DEFAULT_NAS.contains(natureStr)) {
                        continue;
                    }
                    if (DEFAULT_NAS.contains(natureStr) && t.getName().length() < MIN_M_NATURE_LENGTH) {
                        continue;
                    }
                    tagMap.put(t.getName(), natureStr);
                }

                task.setHtmlContent(mainContent);
                task.setContentMap(contentMap);
                task.setResult(result);
                task.setTagMap(tagMap);

                pipe.putTask(task);
            }
            action.setFinish();
        }).start();

        return pipe;
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


    static class DoubanAnalysis {

        static {
            loadDic();
        }

        private static void loadDic() {
            List<String> words = null;

            words = FileUtil.readResources("library/facilities.txt");
            loadDic(words, "facility", 99999);

            words = FileUtil.readResources("library/house.txt");
            loadDic(words, "house", 99999);

            words = FileUtil.readResources("library/subways.txt");
            loadDic(words, "subway", 99999);

            words = FileUtil.readResources("library/streets.txt");
            loadDic(words, "street", 99999);

            words = FileUtil.readResources("library/area.txt");
            loadDic(words, "area", 99999);

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
