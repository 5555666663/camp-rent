package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单（押金/租金）
 *
 * @author camp
 */
@Data
@TableName("payment")
public class Payment {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 支付单号（幂等） */
    private String payNo;

    /** 渠道：alipay_sandbox / wechat_sandbox / mock */
    private String channel;

    /** 1押金 2租金 */
    private Integer payType;

    /** 金额 */
    private BigDecimal amount;

    /** 0待支付 1支付成功 2已退款 3退款失败 */
    private Integer status;

    /** 第三方流水号 */
    private String tradeNo;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 退款时间 */
    private LocalDateTime refundTime;

    /** 回调原文 */
    private String rawJson;

    /** 创建时间 */
    private LocalDateTime createTime;
}
