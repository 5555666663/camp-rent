package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价反馈实体
 *
 * @author camp
 */
@Data
@TableName("comment")
public class Comment implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联订单ID */
    private Long orderId;

    /** 评价用户ID */
    private Long userId;

    /** 关联网点ID */
    private Long storeId;

    /** 评分（1~5） */
    private Integer score;

    /** 评价内容 */
    private String content;

    /** 创建时间 */
    private LocalDateTime createTime;
}
