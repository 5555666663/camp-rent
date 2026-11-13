package com.camp.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 评价请求参数
 *
 * @author camp
 */
@Data
public class CommentDTO implements Serializable {

    /** 订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 评分（1~5） */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer score;

    /** 评价内容 */
    private String content;
}
