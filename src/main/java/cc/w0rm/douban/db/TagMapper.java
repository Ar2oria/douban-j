package cc.w0rm.douban.db;

import cc.w0rm.douban.db.Tag;
import cc.w0rm.douban.db.TagExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface TagMapper {
    long countByExample(TagExample example);

    int deleteByExample(TagExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Tag record);

    int insertSelective(Tag record);

    List<Tag> selectByExampleWithRowbounds(TagExample example, RowBounds rowBounds);

    Tag selectOneByExample(TagExample example);

    List<Tag> selectByExample(TagExample example);

    Tag selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Tag record, @Param("example") TagExample example);

    int updateByExample(@Param("record") Tag record, @Param("example") TagExample example);

    int updateByPrimaryKeySelective(Tag record);

    int updateByPrimaryKey(Tag record);

    int batchInsert(@Param("list") List<Tag> list);

    int batchInsertSelective(@Param("list") List<Tag> list, @Param("selective") Tag.Column ... selective);
}