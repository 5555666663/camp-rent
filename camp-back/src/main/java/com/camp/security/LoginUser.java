package com.camp.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录用户信息（存入 SecurityContext 的 principal）
 *
 * @author camp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 角色：0-普通用户，1-管理员 */
    private Integer role;

    /** 是否为管理员 */
    public boolean isAdmin() {
        return role != null && role == 1;
    }
}
