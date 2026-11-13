package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客服咨询消息实体
 *
 * @author camp
 */
@Data
@TableName("cs_message")
public class CsMessage implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提问用户ID */
    private Long userId;

    /** 提问用户名 */
    private String username;

    /** 咨询内容 */
    private String content;

    /** 客服回复 */
    private String reply;

    /** 状态：0-待处理，1-已回复 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
