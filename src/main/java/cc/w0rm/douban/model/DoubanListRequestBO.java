package cc.w0rm.douban.model;

import cc.w0rm.douban.util.UrlUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.Objects;

@Data
public class DoubanListRequestBO {

    private static final String URL_TEMPLATE = "http://uz.yurixu.com/uz/index/getList?city={city}&order={order}&page={page}&pageSize={pageSize}&key={key}&_={timestamp}";

    @AllArgsConstructor
    @Getter
    enum Order {
        DESC("desc"),
        ASC("asc");

        private final String desc;
    }


    private String city = "beijing";
    private Order order = Order.DESC;
    private Integer page = 1;
    private Integer pageSize = 30;
    private String key;
    private Long timestamp;


    public String getUrl() {
        return URL_TEMPLATE.replace("{city}", city)
                .replace("{order}", order.getDesc())
                .replace("{page}", page.toString())
                .replace("{pageSize}", pageSize.toString())
                .replace("{key}", UrlUtil.encode(StringUtils.isBlank(key) ? "" : key))
                .replace("{timestamp}", "" + (Objects.isNull(timestamp) ? Instant.now().toEpochMilli() : timestamp));

    }

}
