package cc.w0rm.douban.db;

import cc.w0rm.douban.db.Douban;
import cc.w0rm.douban.db.DoubanExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface DoubanMapper {
    long countByExample(DoubanExample example);

    int deleteByExample(DoubanExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Douban record);

    int insertSelective(Douban record);

    List<Douban> selectByExampleWithRowbounds(DoubanExample example, RowBounds rowBounds);

    Douban selectOneByExample(DoubanExample example);

    List<Douban> selectByExample(DoubanExample example);

    Douban selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Douban record, @Param("example") DoubanExample example);

    int updateByExample(@Param("record") Douban record, @Param("example") DoubanExample example);

    int updateByPrimaryKeySelective(Douban record);

    int updateByPrimaryKey(Douban record);

    int batchInsert(@Param("list") List<Douban> list);

    int batchInsertSelective(@Param("list") List<Douban> list, @Param("selective") Douban.Column ... selective);
}