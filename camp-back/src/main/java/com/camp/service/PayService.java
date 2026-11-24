package com.camp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.camp.dto.PayCreateDTO;
import com.camp.entity.Payment;

import java.util.Map;

/**
 * 支付与押金服务（沙箱）
 *
 * @author camp
 */
public interface PayService extends IService<Payment> {

    /** 创建支付单（幂等），返回支付单号与模拟收银台地址 */
    Map<String, Object> create(PayCreateDTO dto);

    /** 沙箱支付成功回调（幂等） */
    boolean notifySuccess(String payNo, String tradeNo);

    /** 查询订单支付单 */
    Map<String, Object> detail(Long orderId);

    /** 押金退款（归还/定损后调用） */
    void refundDeposit(Long orderId, java.math.BigDecimal deductAmount);
}
