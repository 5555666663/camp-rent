package com.camp.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理列表对象（不包含密码）
 *
 * @author camp
 */
@Data
public class UserVO implements Serializable {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 角色：0-普通用户，1-管理员 */
    private Integer role;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 该用户拥有的角色编码集合 */
    private List<String> roles;
}
