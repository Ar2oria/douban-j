create database trick;
use trick;

CREATE TABLE `douban` (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT,
                          `title` varchar(128) CHARACTER SET utf8mb4 NOT NULL DEFAULT '',
                          `url` varchar(128) CHARACTER SET utf8mb4 NOT NULL DEFAULT '',
                          `author` varchar(32) CHARACTER SET utf8mb4 NOT NULL DEFAULT '',
                          `author_url` varchar(128) CHARACTER SET utf8mb4 NOT NULL DEFAULT '',
                          `pub_time` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
                          `create_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `web_id` bigint(20) NOT NULL DEFAULT '0',
                          `content` varchar(4096) CHARACTER SET utf8mb4 NOT NULL DEFAULT '',
                          PRIMARY KEY (`id`),
                          KEY `idx_title` (`title`),
                          KEY `idx_pub_time` (`pub_time`),
                          KEY `idx_author` (`author`),
                          KEY `idx_web_id` (`web_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `tag` (
                       `id` bigint(20) NOT NULL AUTO_INCREMENT,
                       `douban_id` bigint(20) NOT NULL DEFAULT '0',
                       `tag_text` varchar(32) CHARACTER SET utf8mb4 NOT NULL DEFAULT '',
                       `create_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       `nature` varchar(16) CHARACTER SET utf8mb4 NOT NULL DEFAULT '',
                       PRIMARY KEY (`id`),
                       KEY `idx_text` (`tag_text`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;