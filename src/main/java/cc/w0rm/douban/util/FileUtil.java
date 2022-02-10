package cc.w0rm.douban.util;

import org.apache.ibatis.io.Resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author xuyang
 * @date 2022/2/10
 */
public class FileUtil {


    public static List<String> readResources(String resource) {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        Set<String> result = new HashSet<>();
        try {
            inputStream = Resources.getResourceAsStream(resource);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String buffer;
            while ((buffer = bufferedReader.readLine()) != null) {
                result.add(buffer);
            }
            return new ArrayList<>(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (Objects.nonNull(bufferedReader)) {
                    bufferedReader.close();
                }
                if (Objects.nonNull(inputStream)) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}
