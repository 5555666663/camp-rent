package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author camp
 */
@Data
@TableName("user")
public class User implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（登录账号） */
    private String username;

    /** 密码（MD5加密） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 头像地址 */
    private String avatar;

    /** 角色：0-普通用户，1-管理员 */
    private Integer role;

    /** 状态：0-禁用，1-正常 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
