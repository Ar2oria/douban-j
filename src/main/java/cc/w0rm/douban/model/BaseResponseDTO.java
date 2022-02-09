package cc.w0rm.douban.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResponseDTO<T> {
    private Integer code;
    private T data;
    private String msg;

}
