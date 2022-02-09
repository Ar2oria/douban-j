package cc.w0rm.douban;

import cc.w0rm.douban.db.Douban;
import cc.w0rm.douban.db.DoubanMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        InputStream inputStream = Resources.getResourceAsStream("mybatis.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession session = sqlSessionFactory.openSession();
        DoubanMapper mapper = session.getMapper(DoubanMapper.class);
        Douban douban = mapper.selectByPrimaryKey(1L);
        System.out.println(douban);
        session.close();
    }
}
