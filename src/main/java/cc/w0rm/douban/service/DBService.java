package cc.w0rm.douban.service;

import cc.w0rm.douban.db.*;
import cc.w0rm.douban.model.FilterMask;
import cc.w0rm.douban.model.Pipe;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author xuyang
 * @date 2022/2/9
 */
public class DBService {


    static class MapperManager {
        private static final InputStream INPUT_STREAM = createInputStream();
        private static SqlSessionFactory sqlSessionFactory;
        private static SqlSession sqlSession;

        private static InputStream createInputStream() {
            try {
                return Resources.getResourceAsStream("mybatis.xml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        private static <T> T getMapper(Class<T> clazz) {
            return getSqlSession().getMapper(clazz);
        }

        private static synchronized SqlSession getSqlSession() {
            if (Objects.isNull(sqlSessionFactory)) {
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(INPUT_STREAM);
            }
            if (Objects.isNull(sqlSession)) {
                sqlSession = sqlSessionFactory.openSession();
            }
            return sqlSession;
        }
    }


    public List<Douban> selectDoubanByIdList(List<Long> idList) {
        DoubanMapper doubanMapper = MapperManager.getMapper(DoubanMapper.class);
        DoubanExample example = new DoubanExample();
        example.createCriteria()
                .andIdIn(idList);
        return doubanMapper.selectByExample(example);
    }

    public List<Douban> selectDoubanByTagList(List<String> tagList) {
        TagMapper tagMapper = MapperManager.getMapper(TagMapper.class);
        TagExample tagExample = new TagExample();
        tagExample.createCriteria()
                .andTextIn(tagList);
        List<Tag> tags = tagMapper.selectByExample(tagExample);

        return null;
    }


    public FilterMask getFilter() {
        return null;
    }

    public void save(Pipe pipeText) {

    }
}
