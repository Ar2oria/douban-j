package cc.w0rm.douban.service;

import cc.w0rm.douban.db.*;
import cc.w0rm.douban.model.DoubanAnalyseModel;
import cc.w0rm.douban.model.DoubanListResponseDTO;
import cc.w0rm.douban.model.FilterMask;
import cc.w0rm.douban.model.Pipe;
import cc.w0rm.douban.util.CollUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
                sqlSession = sqlSessionFactory.openSession(true);
            }
            return sqlSession;
        }
    }


    public long saveDouban(DoubanAnalyseModel task) {
        if (Objects.isNull(task)) {
            return 0;
        }

        DoubanMapper doubanMapper = MapperManager.getMapper(DoubanMapper.class);
        TagMapper tagMapper = MapperManager.getMapper(TagMapper.class);

        DoubanListResponseDTO.ItemDTO item = task.getItem();

        long webId = getWebId(item.getUrl());

        Douban douban = new Douban();
        douban.setTitle(item.getTitle());
        douban.setAuthor(item.getAuthor());
        douban.setAuthorUrl(item.getAuthorUrl());
        douban.setUrl(item.getUrl());
        douban.setPubTime(item.getPubTime());
        douban.setWebId(webId);
        douban.setContent(task.getHtmlContent());
        douban.setPredictPrice(task.getPrice());

        Douban oldDouban = selectDoubanByWebId(webId);
        if (Objects.nonNull(oldDouban)) {
            if (StringUtils.isBlank(task.getHtmlContent())) {
                return oldDouban.getId();
            }
            douban.setId(oldDouban.getId());
            doubanMapper.updateByPrimaryKeySelective(douban);
        } else {
            doubanMapper.insertSelective(douban);
        }

        if (CollUtil.isEmpty(task.getTagMap())) {
            return douban.getId();
        }

        if (StringUtils.isBlank(task.getHtmlContent())) {
            return douban.getId();
        } else {
            TagExample example = new TagExample();
            example.createCriteria()
                    .andDoubanIdEqualTo(douban.getId());
            tagMapper.deleteByExample(example);
        }

        List<Tag> tagEntityList = task.getTagMap().keySet().stream()
                .map(tag -> {
                    Tag t = new Tag();
                    t.setDoubanId(douban.getId());
                    t.setTagText(tag);
                    t.setNature(task.getTagMap().get(tag));
                    return t;
                }).collect(Collectors.toList());
        tagMapper.batchInsertSelective(tagEntityList, Tag.Column.doubanId, Tag.Column.tagText, Tag.Column.nature);

        return douban.getId();
    }

    public List<Douban> selectDoubanByPlace(String place) {
        if (StringUtils.isBlank(place)) {
            return Collections.emptyList();
        }
        TagMapper tagMapper = MapperManager.getMapper(TagMapper.class);
        TagExample example = new TagExample();
        example.createCriteria()
                .andTagTextLike(place + "%");
        List<Tag> tags = tagMapper.selectByExample(example);
        if (CollUtil.isEmpty(tags)) {
            return Collections.emptyList();
        }

        return selectDoubanByTagList(tags);
    }

    public List<Tag> selectTagByDoubanIdList(List<Long> doubanIdList) {
        TagMapper tagMapper = MapperManager.getMapper(TagMapper.class);
        TagExample example = new TagExample();
        example.createCriteria()
                .andDoubanIdIn(doubanIdList);
        return tagMapper.selectByExample(example);
    }

    public List<Douban> selectDoubanByWebIdList(List<Long> webIdList) {
        DoubanMapper doubanMapper = MapperManager.getMapper(DoubanMapper.class);
        DoubanExample example = new DoubanExample();
        example.createCriteria()
                .andWebIdIn(webIdList);
        return doubanMapper.selectByExample(example);
    }

    public Douban selectDoubanByWebId(long webId) {
        DoubanMapper doubanMapper = MapperManager.getMapper(DoubanMapper.class);
        DoubanExample example = new DoubanExample();
        example.createCriteria()
                .andWebIdEqualTo(webId);
        return doubanMapper.selectByExample(example).stream()
                .findFirst().orElse(null);
    }


    public List<Douban> selectDoubanByIdList(List<Long> idList) {
        DoubanMapper doubanMapper = MapperManager.getMapper(DoubanMapper.class);
        DoubanExample example = new DoubanExample();
        example.createCriteria()
                .andIdIn(idList);
        return doubanMapper.selectByExample(example);
    }

    public List<Douban> selectDoubanByTagTextList(List<String> tagList) {
        TagMapper tagMapper = MapperManager.getMapper(TagMapper.class);
        TagExample tagExample = new TagExample();
        tagExample.createCriteria()
                .andTagTextIn(tagList);
        List<Tag> tags = tagMapper.selectByExample(tagExample);
        if (CollUtil.isEmpty(tags)) {
            return Collections.emptyList();
        }

        return selectDoubanByTagList(tags);
    }

    private List<Douban> selectDoubanByTagList(List<Tag> tags) {
        if (CollUtil.isEmpty(tags)) {
            return Collections.emptyList();
        }
        List<Long> doubanIdList = tags.stream()
                .map(Tag::getDoubanId)
                .collect(Collectors.toList());

        return selectDoubanByIdList(doubanIdList);
    }


    static class DoubanFilter implements FilterMask<DoubanListResponseDTO.ItemDTO> {
        private DBService dbService;

        public DoubanFilter() {
            dbService = new DBService();
        }

        @Override
        public Map<DoubanListResponseDTO.ItemDTO, Boolean> filter(List<DoubanListResponseDTO.ItemDTO> itemList) {
            if (CollUtil.isEmpty(itemList)) {
                return Collections.emptyMap();
            }

            Map<Long, DoubanListResponseDTO.ItemDTO> itemMap = itemList.stream()
                    .collect(Collectors.toMap(item -> getWebId(item.getUrl()), Function.identity()));
            Set<Long> webIds = itemMap.keySet();

            List<Douban> doubanList = dbService.selectDoubanByWebIdList(new ArrayList<>(webIds));
            Set<Long> existWebIds = doubanList.stream()
                    .filter(douban -> StringUtils.isNotBlank(douban.getContent()))
                    .map(Douban::getWebId)
                    .collect(Collectors.toSet());

            return itemMap.keySet().stream()
                    .collect(Collectors.toMap(itemMap::get, existWebIds::contains));
        }
    }

    private static final Pattern WEB_ID_PATTERN = Pattern.compile("/(\\d+?)/");

    private static long getWebId(String url) {
        if (StringUtils.isBlank(url)) {
            return 0L;
        }
        Matcher matcher = WEB_ID_PATTERN.matcher(url);
        if (!matcher.find()) {
            return 0L;
        }

        return Long.parseLong(matcher.group(1));
    }

    public FilterMask<DoubanListResponseDTO.ItemDTO> getFilter() {
        return new DoubanFilter();
    }

    public void save(Pipe<DoubanAnalyseModel> doubanAnalyseModelPipe) {
        new Thread(() -> {
            while (!doubanAnalyseModelPipe.finished()) {
                DoubanAnalyseModel task = doubanAnalyseModelPipe.getTask();
                if (Objects.isNull(task)) {
                    return;
                }

                try {
                    saveDouban(task);
                    System.out.println("处理成功:" + task.getItem().getUrl());
                } catch (Exception e) {
                    System.out.println("保存失败:" + task.getItem().getUrl());
                    e.printStackTrace();
                }
            }
            System.out.println("=== 数据库 所有数据处理完成 ===");
        }).start();
    }
}
