package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内消息
 *
 * @author camp
 */
@Data
@TableName("message")
public class Message {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户 */
    private Long userId;

    /** 1到期提醒 2逾期提醒 3系统通知 4售后 */
    private Integer type;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 关联业务ID（如订单ID） */
    private Long bizId;

    /** 0未读 1已读 */
    private Integer readFlag;

    /** 创建时间 */
    private LocalDateTime createTime;
}
