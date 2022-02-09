package cc.w0rm.douban.db;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

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