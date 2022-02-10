package cc.w0rm.douban.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {


    public static String format(Date date) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return simpleDateFormat.format(date);

    }

    public static int msToDays(long ms) {
        return (int) (ms / 24 / 60 / 60 / 1000);
    }

}
