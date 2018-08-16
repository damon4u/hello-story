# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.7.9)
# Database: music-story
# Generation Time: 2018-08-16 13:19:06 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table comment
# ------------------------------------------------------------

DROP TABLE IF EXISTS `comment`;

CREATE TABLE `comment` (
  `comment_id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '评论id',
  `song_id` bigint(20) DEFAULT NULL COMMENT '歌曲id',
  `liked_count` bigint(20) DEFAULT NULL COMMENT '点赞数量',
  `content` varchar(500) DEFAULT NULL COMMENT '评论内容',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `be_replied_user_id` bigint(20) DEFAULT NULL COMMENT '引用评论用户id',
  `be_replied_content` varchar(500) DEFAULT NULL COMMENT '引用评论内容',
  `comment_time` datetime DEFAULT NULL COMMENT '评论时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`comment_id`),
  KEY `idx_song` (`song_id`),
  KEY `idx_likedcount` (`liked_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



# Dump of table song
# ------------------------------------------------------------

DROP TABLE IF EXISTS `song`;

CREATE TABLE `song` (
  `song_id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '歌曲id',
  `name` varchar(100) DEFAULT NULL COMMENT '歌曲名称',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `image` varchar(200) DEFAULT NULL COMMENT '封面',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



# Dump of table user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `user_id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `nickname` varchar(100) NOT NULL DEFAULT '' COMMENT '昵称',
  `avatar_url` varchar(200) NOT NULL DEFAULT '' COMMENT '头像',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
