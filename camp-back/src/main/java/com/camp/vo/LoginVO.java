package com.camp.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 登录返回对象（用户信息 + token）
 *
 * @author camp
 */
@Data
public class LoginVO implements Serializable {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 头像 */
    private String avatar;

    /** 角色：0-普通用户，1-管理员 */
    private Integer role;

    /** 用户拥有的角色编码集合，如 [ROLE_USER]、[ROLE_CS]、[ROLE_ADMIN]（可多角色） */
    private List<String> roles;

    /** 登录 token */
    private String token;
}
