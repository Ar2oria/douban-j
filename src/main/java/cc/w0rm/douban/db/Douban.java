package cc.w0rm.douban.db;

import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
@ToString
public class Douban {
    private Long id;

    private String title;

    private String url;

    private String author;

    private String authorUrl;

    private Date pubTime;

    private Date createAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author == null ? null : author.trim();
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl == null ? null : authorUrl.trim();
    }

    public Date getPubTime() {
        return pubTime;
    }

    public void setPubTime(Date pubTime) {
        this.pubTime = pubTime;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public static Douban.Builder builder() {
        return new Douban.Builder();
    }

    public static class Builder {
        private Douban obj;

        public Builder() {
            this.obj = new Douban();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder title(String title) {
            obj.setTitle(title);
            return this;
        }

        public Builder url(String url) {
            obj.setUrl(url);
            return this;
        }

        public Builder author(String author) {
            obj.setAuthor(author);
            return this;
        }

        public Builder authorUrl(String authorUrl) {
            obj.setAuthorUrl(authorUrl);
            return this;
        }

        public Builder pubTime(Date pubTime) {
            obj.setPubTime(pubTime);
            return this;
        }

        public Builder createAt(Date createAt) {
            obj.setCreateAt(createAt);
            return this;
        }

        public Douban build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        title("title", "title", "VARCHAR", false),
        url("url", "url", "VARCHAR", false),
        author("author", "author", "VARCHAR", false),
        authorUrl("author_url", "authorUrl", "VARCHAR", false),
        pubTime("pub_time", "pubTime", "TIMESTAMP", false),
        createAt("create_at", "createAt", "TIMESTAMP", false);

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