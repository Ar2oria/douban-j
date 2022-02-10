package cc.w0rm.douban.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Tag {
    private Long id;

    private Long doubanId;

    private String tagText;

    private Date createAt;

    private String nature;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoubanId() {
        return doubanId;
    }

    public void setDoubanId(Long doubanId) {
        this.doubanId = doubanId;
    }

    public String getTagText() {
        return tagText;
    }

    public void setTagText(String tagText) {
        this.tagText = tagText == null ? null : tagText.trim();
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature == null ? null : nature.trim();
    }

    public static Tag.Builder builder() {
        return new Tag.Builder();
    }

    public static class Builder {
        private Tag obj;

        public Builder() {
            this.obj = new Tag();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder doubanId(Long doubanId) {
            obj.setDoubanId(doubanId);
            return this;
        }

        public Builder tagText(String tagText) {
            obj.setTagText(tagText);
            return this;
        }

        public Builder createAt(Date createAt) {
            obj.setCreateAt(createAt);
            return this;
        }

        public Builder nature(String nature) {
            obj.setNature(nature);
            return this;
        }

        public Tag build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        doubanId("douban_id", "doubanId", "BIGINT", false),
        tagText("tag_text", "tagText", "VARCHAR", false),
        createAt("create_at", "createAt", "TIMESTAMP", false),
        nature("nature", "nature", "VARCHAR", false);

        private static final String BEGINNING_DELIMITER = "\"";

        private static final String ENDING_DELIMITER = "\"";

        private final String column;

        private final boolean isColumnNameDelimited;

        private final String javaProperty;

        private final String jdbcType;

        public String value() {
            return this.column;
        }

        public String getValue() {
            return this.column;
        }

        public String getJavaProperty() {
            return this.javaProperty;
        }

        public String getJdbcType() {
            return this.jdbcType;
        }

        Column(String column, String javaProperty, String jdbcType, boolean isColumnNameDelimited) {
            this.column = column;
            this.javaProperty = javaProperty;
            this.jdbcType = jdbcType;
            this.isColumnNameDelimited = isColumnNameDelimited;
        }

        public String desc() {
            return this.getEscapedColumnName() + " DESC";
        }

        public String asc() {
            return this.getEscapedColumnName() + " ASC";
        }

        public static Column[] excludes(Column ... excludes) {
            ArrayList<Column> columns = new ArrayList<>(Arrays.asList(Column.values()));
            if (excludes != null && excludes.length > 0) {
                columns.removeAll(new ArrayList<>(Arrays.asList(excludes)));
            }
            return columns.toArray(new Column[]{});
        }

        public static Column[] all() {
            return Column.values();
        }

        public String getEscapedColumnName() {
            if (this.isColumnNameDelimited) {
                return new StringBuilder().append(BEGINNING_DELIMITER).append(this.column).append(ENDING_DELIMITER).toString();
            } else {
                return this.column;
            }
        }

        public String getAliasedEscapedColumnName() {
            return this.getEscapedColumnName();
        }
    }
}