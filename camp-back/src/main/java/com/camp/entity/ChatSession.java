package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客服会话实体（一个用户与客服之间只保留一个会话）
 *
 * @author camp
 */
@Data
@TableName("chat_session")
public class ChatSession {

    /** 会话ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户账号 */
    private String username;

    /** 最后一条消息内容（会话列表展示） */
    private String lastContent;

    /** 最后一条消息时间 */
    private LocalDateTime lastTime;

    /** 用户未读消息数 */
    private Integer userUnread;

    /** 客服未读消息数 */
    private Integer csUnread;

    /** 0 进行中 1 已结束 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
