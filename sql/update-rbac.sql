-- ============================================================
-- 露营装备租赁系统 - RBAC 角色权限（普通用户 / 客服 / 超级管理员）
-- 可重复执行
-- ============================================================
USE camp_rent;

DROP TABLE IF EXISTS cs_message;
DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS role;

-- 1. 角色表
CREATE TABLE role (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_code   VARCHAR(50) NOT NULL COMMENT '角色编码：ROLE_USER/ROLE_CS/ROLE_ADMIN',
    role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '角色说明',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='角色表';

-- 2. 用户角色关联表（一个用户可绑定多个角色）
CREATE TABLE user_role (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT   NOT NULL COMMENT '用户ID',
    role_id     BIGINT   NOT NULL COMMENT '角色ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='用户角色关联表';

-- 3. 客服咨询消息表
CREATE TABLE cs_message (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT       NOT NULL COMMENT '提问用户ID',
    username    VARCHAR(50)  DEFAULT NULL COMMENT '提问用户名',
    content     VARCHAR(500) NOT NULL COMMENT '咨询内容',
    reply       VARCHAR(500) DEFAULT NULL COMMENT '客服回复',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-已回复',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='客服咨询消息表';

-- 4. 初始化角色
INSERT INTO role (id, role_code, role_name, description)
VALUES (1, 'ROLE_USER', '普通用户', '天气组套、自定义套餐、下单、查看个人订单'),
       (2, 'ROLE_CS', '客服', '查看全部订单、修改订单状态、处理售后、查看咨询消息'),
       (3, 'ROLE_ADMIN', '超级管理员', '拥有全部后台权限');

-- 5. 初始化用户角色（admin=超级管理员，zhangsan/lisi=普通用户）
INSERT INTO user_role (user_id, role_id)
VALUES (1, 3),
       (2, 1),
       (3, 1);

-- 6. 新增客服账号 kefu / 123456（密码为 MD5）
INSERT INTO user (id, username, password, real_name, phone, role, status, create_time)
VALUES (4, 'kefu', 'e10adc3949ba59abbe56e057f20f883e', '客服小美', '13800000004', 0, 1, NOW())
ON DUPLICATE KEY UPDATE real_name = '客服小美';

INSERT INTO user_role (user_id, role_id)
SELECT 4, 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_role WHERE user_id = 4 AND role_id = 2);

-- 校验
SELECT u.id, u.username, GROUP_CONCAT(r.role_code) AS roles
FROM user u
         LEFT JOIN user_role ur ON ur.user_id = u.id
         LEFT JOIN role r ON r.id = ur.role_id
GROUP BY u.id, u.username
ORDER BY u.id;
