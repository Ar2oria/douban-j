package cc.w0rm.douban.model;

import java.util.List;
import java.util.Map;

/**
 * @author xuyang
 * @date 2022/2/9
 */
public interface FilterMask<T> {

    Map<T, Boolean> filter(List<T> itemList);

}
