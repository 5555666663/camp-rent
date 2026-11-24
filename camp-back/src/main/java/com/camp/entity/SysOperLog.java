package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统操作审计日志
 *
 * @author camp
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人ID */
    private Long userId;

    /** 操作人账号 */
    private String username;

    /** 角色编码 */
    private String roleCode;

    /** 模块 */
    private String module;

    /** 操作 */
    private String action;

    /** 请求方法 */
    private String method;

    /** 请求地址 */
    private String uri;

    /** 请求参数（脱敏） */
    private String params;

    /** 返回码 */
    private Integer resultCode;

    /** 异常信息 */
    private String errorMsg;

    /** 耗时（毫秒） */
    private Long costMs;

    /** 客户端 IP */
    private String ip;

    /** 创建时间 */
    private LocalDateTime createTime;
}
