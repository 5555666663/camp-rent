-- ============================================================
-- 咨询（意见反馈）功能增量脚本：cs_message 增加 title 字段
-- 可重复执行
-- ============================================================
USE camp_rent;

DROP PROCEDURE IF EXISTS add_col_if_missing;
DELIMITER $$
CREATE PROCEDURE add_col_if_missing(IN tbl VARCHAR(64), IN col VARCHAR(64), IN ddl VARCHAR(255))
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col) THEN
    SET @s = CONCAT('ALTER TABLE ', tbl, ' ADD COLUMN ', ddl);
    PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
  END IF;
END$$
DELIMITER ;

CALL add_col_if_missing('cs_message', 'title', 'title VARCHAR(100) NULL COMMENT ''咨询标题'' AFTER username');
DROP PROCEDURE IF EXISTS add_col_if_missing;

-- 演示数据（若表中没有历史数据则补两条，便于页面演示）
INSERT INTO cs_message (user_id, username, title, content, reply, status, create_time)
SELECT 2, 'zhangsan', '帐篷押金什么时候退', '我上周归还了双人帐篷，押金 200 元什么时候退回？', NULL, 0, NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cs_message);

INSERT INTO cs_message (user_id, username, title, content, reply, status, create_time)
SELECT 3, 'lisi', '可以跨网点归还吗', '我在北京望京取的睡袋，能不能在上海浦东归还？', '可以，下单时选择归还网点即可，库存会回补到归还网点。', 1, NOW()
FROM DUAL WHERE (SELECT COUNT(*) FROM cs_message) < 2;

SELECT id, username, title, status, create_time FROM cs_message ORDER BY id;
