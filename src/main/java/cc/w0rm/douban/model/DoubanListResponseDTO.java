package cc.w0rm.douban.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoubanListResponseDTO {

    private List<ItemDTO> dataList;

    private PageInfoDTO pageInfo;


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemDTO {
        private String author;
        @JsonProperty("author_url")
        private String authorUrl;
        private String id;
        @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
        @JsonProperty("pub_time")
        private Date pubTime;
        @JsonProperty("source_name")
        private String sourceName;
        private String title;
        private String url;
    }


}
