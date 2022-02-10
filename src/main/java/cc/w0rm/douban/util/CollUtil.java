package cc.w0rm.douban.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author xuyang
 * @date 2022/2/10
 */
public class CollUtil {

    public static <T> boolean isEmpty(Collection<T> collection) {
        if (Objects.isNull(collection) || collection.isEmpty()) {
            return true;
        }
        return false;
    }

    public static <T, K> boolean isEmpty(Map<T, K> map) {
        if (Objects.isNull(map) || map.isEmpty()) {
            return true;
        }
        return false;
    }

}
