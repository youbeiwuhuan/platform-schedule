SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for t_platform_appinfo
-- ----------------------------
DROP TABLE IF EXISTS `t_platform_appinfo`;
CREATE TABLE `t_platform_appinfo` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_name` varchar(50) NOT NULL COMMENT '应用名称',
  `app_key` varchar(50) NOT NULL,
  `app_secret` varchar(50) NOT NULL,
  `status` tinyint(4) DEFAULT '0' COMMENT '0 : 有效  /  1: 无效',
  `remark` varchar(255) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_app_name_key` (`app_name`) USING BTREE COMMENT 'app_name 全局唯一'
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_platform_namesrv
-- ----------------------------
DROP TABLE IF EXISTS `t_platform_namesrv`;
CREATE TABLE `t_platform_namesrv` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键Id',
  `namesrv_ip` varchar(80) NOT NULL COMMENT 'namesrv ip ;分隔',
  `status` tinyint(4) NOT NULL COMMENT '状态 0：有效 1：无效',
  `type` tinyint(4) DEFAULT NULL COMMENT '0: 任务调度 ',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_namesrv_ip_key` (`namesrv_ip`) USING BTREE COMMENT 'ip 唯一索引'
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_schedule_job_info
-- ----------------------------
DROP TABLE IF EXISTS `t_schedule_job_info`;
CREATE TABLE `t_schedule_job_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL COMMENT '应用编号',
  `app_name` varchar(80) COLLATE utf8_bin NOT NULL COMMENT '应用名称',
  `job_name` varchar(80) COLLATE utf8_bin NOT NULL COMMENT '任务名称',
  `job_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '任务类别\n0: 基础定时任务',
  `job_description` varchar(255) COLLATE utf8_bin DEFAULT '' COMMENT '任务描述',
  `job_cron` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT 'cron表达式',
  `job_param` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '任务参数',
  `job_handler` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '任务处理器 当前job_type:0 时，默认是java类上的注解',
  `route_mode` tinyint(255) DEFAULT '0' COMMENT '是否广播模式 0：不是 1： 是',
  `execute_timeout` int(11) DEFAULT '0' COMMENT '执行超时时间',
  `fail_retry_count` tinyint(4) DEFAULT '0' COMMENT '失败尝试次数 ',
  `author` varchar(50) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '负责人',
  `alarm_email` varchar(255) COLLATE utf8_bin DEFAULT '' COMMENT '报警邮箱',
  `status` tinyint(4) DEFAULT '0' COMMENT ' 0: 有效 1: 无效 ',
  `trigger_next_time` datetime DEFAULT NULL COMMENT '下次触发时间',
  `trigger_last_time` datetime DEFAULT NULL COMMENT '最后触发时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `normal_create_time_key` (`create_time`) USING BTREE COMMENT '创建时间',
  KEY `normal_app_id_key` (`app_id`) USING BTREE COMMENT '应用id',
  KEY `unique_job_name` (`job_name`) USING BTREE COMMENT '任务名称'
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for t_schedule_job_lock
-- ----------------------------
DROP TABLE IF EXISTS `t_schedule_job_lock`;
CREATE TABLE `t_schedule_job_lock` (
  `lock_name` varchar(50) NOT NULL COMMENT '锁名称',
  PRIMARY KEY (`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_schedule_job_log
-- ----------------------------
DROP TABLE IF EXISTS `t_schedule_job_log`;
CREATE TABLE `t_schedule_job_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_id` bigint(20) NOT NULL COMMENT '任务id',
  `app_id` bigint(20) DEFAULT NULL COMMENT '应用id 应用表主键',
  `trigger_status` tinyint(4) NOT NULL DEFAULT '-1' COMMENT '-1: 待触发\n0: 触发成功\n1: 触发失败',
  `trigger_time` datetime DEFAULT NULL COMMENT '触发时间',
  `trigger_message` varchar(255) COLLATE utf8_bin DEFAULT '' COMMENT '触发内容',
  `callback_time` datetime DEFAULT NULL COMMENT '回调时间',
  `callback_status` tinyint(4) DEFAULT '-1' COMMENT '回调状态 0：成功 1：失败 2： 部分失败 -1 : 未回调 ',
  `callback_message` varchar(255) COLLATE utf8_bin DEFAULT '' COMMENT '回调内容',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `normal_job_id_key` (`job_id`) USING BTREE COMMENT '任务id',
  KEY `normal_app_id_key` (`app_id`) USING BTREE COMMENT '应用id',
  KEY `normal_create_time_key` (`create_time`) USING BTREE COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

BEGIN;
INSERT INTO `t_schedule_job_lock` VALUES ('schedule_lock');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
