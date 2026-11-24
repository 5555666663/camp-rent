package com.camp.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创建支付单入参
 *
 * @author camp
 */
@Data
public class PayCreateDTO {

    /** 订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 渠道，默认 mock（沙箱模拟） */
    private String channel;
}
