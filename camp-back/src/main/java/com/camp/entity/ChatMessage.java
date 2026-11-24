package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客服聊天消息实体
 *
 * @author camp
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    /** 消息ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long sessionId;

    /** 发送方类型：1 用户 2 客服 */
    private Integer senderType;

    /** 发送人ID */
    private Long senderId;

    /** 发送人显示名 */
    private String senderName;

    /** 消息内容 */
    private String content;

    /** 发送时间 */
    private LocalDateTime createTime;
}
