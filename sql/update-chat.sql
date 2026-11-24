-- ============================================================
-- 在线客服聊天（淘宝式会话）表结构
-- 说明：一个用户与客服之间只有一个会话（chat_session），
--       会话内多条消息（chat_message）按时间排序，形成聊天气泡。
-- 可重复执行（先判断表是否存在）
-- ============================================================
USE camp_rent;

CREATE TABLE IF NOT EXISTS `chat_session` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
  `username`      VARCHAR(50)  DEFAULT NULL COMMENT '用户账号',
  `last_content`  VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息内容',
  `last_time`     DATETIME     DEFAULT NULL COMMENT '最后一条消息时间',
  `user_unread`   INT          NOT NULL DEFAULT 0 COMMENT '用户未读数',
  `cs_unread`     INT          NOT NULL DEFAULT 0 COMMENT '客服未读数',
  `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0进行中 1已结束',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user` (`user_id`),
  KEY `idx_last_time` (`last_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服会话表';

CREATE TABLE IF NOT EXISTS `chat_message` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id`  BIGINT       NOT NULL COMMENT '会话ID',
  `sender_type` TINYINT      NOT NULL COMMENT '1用户 2客服',
  `sender_id`   BIGINT       DEFAULT NULL COMMENT '发送人ID',
  `sender_name` VARCHAR(50)  DEFAULT NULL COMMENT '发送人显示名',
  `content`     VARCHAR(1000) NOT NULL COMMENT '消息内容',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服聊天消息表';

-- 校验
SELECT 'chat_session' AS tbl, COUNT(*) AS cnt FROM chat_session
UNION ALL
SELECT 'chat_message' AS tbl, COUNT(*) AS cnt FROM chat_message;
