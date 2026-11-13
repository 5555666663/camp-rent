package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体
 *
 * @author camp
 */
@Data
@TableName("role")
public class Role implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码：ROLE_USER / ROLE_CS / ROLE_ADMIN */
    private String roleCode;

    /** 角色名称 */
    private String roleName;

    /** 角色说明 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createTime;
}
