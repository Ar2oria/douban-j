package cc.w0rm.douban.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageInfoDTO {

    private Integer page;
    private Integer size;
    private Integer total;

}
