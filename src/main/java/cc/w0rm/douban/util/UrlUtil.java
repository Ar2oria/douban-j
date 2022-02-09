package cc.w0rm.douban.util;

import java.net.URLEncoder;
import java.util.Objects;

public class UrlUtil {


    public static String encode(String s) {
        if (Objects.isNull(s)) {
            return "";
        }
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
