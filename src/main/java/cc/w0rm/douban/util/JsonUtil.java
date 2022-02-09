package cc.w0rm.douban.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> T toObject(String s, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(s, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T toObject(String s, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(s, typeReference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
