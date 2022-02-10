package cc.w0rm.douban.model;

import lombok.Data;
import org.ansj.domain.Result;

import java.util.Map;

/**
 * @author xuyang
 * @date 2022/2/10
 */
@Data
public class DoubanAnalyseModel {
    private DoubanListResponseDTO.ItemDTO item;
    private String html;
    private String htmlContent;
    private Map<String, Object> contentMap;
    private Result result;
    private Map<String, String> tagMap;
}
