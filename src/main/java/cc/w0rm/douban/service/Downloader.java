package cc.w0rm.douban.service;

import cc.w0rm.douban.model.*;
import cc.w0rm.douban.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.jsonldjava.utils.JsonUtils;
import okhttp3.*;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.AmbiguityLibrary;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author xuyang
 * @date 2022/2/9
 */
public class Downloader {


    static class DoubanSearchClient {

        private static final OkHttpClient OK_HTTP_CLIENT = buildClient();

        private static OkHttpClient buildClient() {
            ConnectionPool connectionPool = new ConnectionPool(256, 5, TimeUnit.MINUTES);

            return new OkHttpClient.Builder()
                    .callTimeout(3, TimeUnit.SECONDS)
                    .connectionPool(connectionPool)
                    .retryOnConnectionFailure(false)
                    .build();
        }

        DoubanListResponseDTO getDoubanList(DoubanListRequestBO requestBO) {
            Response response = null;
            try {
                Request req = new Request.Builder()
                        .url(requestBO.getUrl())
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Host", "uz.yurixu.com")
                        .addHeader("Referer", "http://uz.yurixu.com/manage/beijing.php")
                        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36")
                        .build();
                Call call = OK_HTTP_CLIENT.newCall(req);
                response = call.execute();
                String responseStr = response.body().string();
                BaseResponseDTO<DoubanListResponseDTO> responseDTO = JsonUtil.toObject(responseStr, new TypeReference<BaseResponseDTO<DoubanListResponseDTO>>() {
                });
                if (Objects.isNull(responseDTO.getCode()) && responseDTO.getCode() != 0) {
                    throw new RuntimeException("请求返回值不为0!");
                }
                return responseDTO.getData();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (Objects.nonNull(response)) {
                    response.close();
                }
            }
        }

        String getDoubanHtml(String url) {
            Response response = null;
            try {
                Request req = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36")
                        .build();
                Call call = OK_HTTP_CLIENT.newCall(req);
                response = call.execute();
                return response.body().string();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (Objects.nonNull(response)) {
                    response.close();
                }
            }
        }
    }

    public static void main(String[] args) {
        DoubanSearchClient doubanSearchClient = new DoubanSearchClient();

        DoubanListRequestBO requestBO = new DoubanListRequestBO();
        requestBO.setKey("望京");

        DoubanListResponseDTO responseDTO = doubanSearchClient.getDoubanList(requestBO);
        System.out.println(responseDTO);

        String doubanHtml = doubanSearchClient.getDoubanHtml(responseDTO.getDataList().get(0).getUrl());
        System.out.println(doubanHtml);

        Pattern pattern = Pattern.compile("<script type=\"application/ld[+]json\">([\\s\\S]*?)</script>");
        Matcher matcher = pattern.matcher(doubanHtml);
        if (!matcher.find()){
            return;
        }

        String s = matcher.group(1).replaceAll("\\s+", "");
        Map o = (Map) JsonUtil.toObject(s, Object.class);
        String text = o.get("text").toString();
        System.out.println(text);



        Result parse = ToAnalysis.parse(text);
        List<Term> ll = parse.getTerms().stream()
                .filter(term -> term.getNatureStr().contains("n"))
                .collect(Collectors.toList());

        System.out.println(ll);

    }


    public static Pipe search(String place, FilterMask filterMask) {


        return null;
    }
}
