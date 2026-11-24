package com.camp.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.exception.BusinessException;
import com.camp.dto.PayCreateDTO;
import com.camp.entity.DepositRecord;
import com.camp.entity.Order;
import com.camp.entity.Payment;
import com.camp.mapper.PaymentMapper;
import com.camp.service.DepositRecordService;
import com.camp.service.OrderService;
import com.camp.service.PayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付与押金服务实现（沙箱模拟通道）
 *
 * @author camp
 */
@Service
public class PayServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PayService {

    @Resource
    private OrderService orderService;
    @Resource
    private DepositRecordService depositRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> create(PayCreateDTO dto) {
        Order order = orderService.getById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() != 0) {
            throw new BusinessException("订单当前状态无需支付");
        }
        String channel = dto.getChannel() == null || dto.getChannel().isEmpty() ? "mock" : dto.getChannel();

        Payment depositPay = getOrCreate(order, channel, 1, order.getDepositMoney());
        Payment rentPay = getOrCreate(order, channel, 2, order.getTotalPrice());

        Map<String, Object> result = new HashMap<>();
        result.put("depositPayNo", depositPay.getPayNo());
        result.put("rentPayNo", rentPay.getPayNo());
        result.put("depositAmount", depositPay.getAmount());
        result.put("rentAmount", rentPay.getAmount());
        // 沙箱收银台：直接调用该地址即视为支付成功
        result.put("payUrl", "/api/pay/mock/" + depositPay.getPayNo());
        result.put("channel", channel);
        return result;
    }

    /** 按 订单+类型 取或建支付单（幂等） */
    private Payment getOrCreate(Order order, String channel, int payType, BigDecimal amount) {
        List<Payment> list = list(Wrappers.<Payment>lambdaQuery()
                .eq(Payment::getOrderId, order.getId())
                .eq(Payment::getPayType, payType)
                .orderByDesc(Payment::getId));
        if (!list.isEmpty() && list.get(0).getStatus() != null && list.get(0).getStatus() < 2) {
            return list.get(0);
        }
        Payment pay = new Payment();
        pay.setOrderId(order.getId());
        pay.setPayNo("P" + order.getId() + payType + IdUtil.getSnowflakeNextIdStr());
        pay.setChannel(channel);
        pay.setPayType(payType);
        pay.setAmount(amount == null ? BigDecimal.ZERO : amount);
        pay.setStatus(0);
        pay.setCreateTime(LocalDateTime.now());
        save(pay);
        return pay;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean notifySuccess(String payNo, String tradeNo) {
        Payment pay = getOne(Wrappers.<Payment>lambdaQuery().eq(Payment::getPayNo, payNo), false);
        if (pay == null) {
            throw new BusinessException("支付单不存在");
        }
        if (pay.getStatus() != null && pay.getStatus() >= 1) {
            return true; // 幂等：已处理
        }
        pay.setStatus(1);
        pay.setPayTime(LocalDateTime.now());
        pay.setTradeNo(tradeNo == null ? "SANDBOX" + System.currentTimeMillis() : tradeNo);
        updateById(pay);

        // 押金冻结
        if (pay.getPayType() != null && pay.getPayType() == 1) {
            DepositRecord record = depositRecordService.getOne(
                    Wrappers.<DepositRecord>lambdaQuery().eq(DepositRecord::getOrderId, pay.getOrderId()), false);
            if (record != null) {
                record.setDepositStatus(1);
                record.setFrozenTime(LocalDateTime.now());
                depositRecordService.updateById(record);
            }
        }
        // 押金与租金均已支付 → 订单进入租赁中
        long paid = count(Wrappers.<Payment>lambdaQuery()
                .eq(Payment::getOrderId, pay.getOrderId()).eq(Payment::getStatus, 1));
        if (paid >= 2) {
            Order order = orderService.getById(pay.getOrderId());
            if (order != null && order.getOrderStatus() != null && order.getOrderStatus() == 0) {
                order.setOrderStatus(1);
                order.setCheckinTime(LocalDateTime.now());
                orderService.updateById(order);
            }
        }
        return true;
    }

    @Override
    public Map<String, Object> detail(Long orderId) {
        List<Payment> list = list(Wrappers.<Payment>lambdaQuery()
                .eq(Payment::getOrderId, orderId).orderByAsc(Payment::getPayType));
        DepositRecord record = depositRecordService.getOne(
                Wrappers.<DepositRecord>lambdaQuery().eq(DepositRecord::getOrderId, orderId), false);
        Map<String, Object> result = new HashMap<>();
        result.put("payments", list);
        result.put("depositRecord", record);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundDeposit(Long orderId, BigDecimal deductAmount) {
        DepositRecord record = depositRecordService.getOne(
                Wrappers.<DepositRecord>lambdaQuery().eq(DepositRecord::getOrderId, orderId), false);
        if (record == null) {
            return;
        }
        BigDecimal deposit = record.getDepositMoney() == null ? BigDecimal.ZERO : record.getDepositMoney();
        BigDecimal deduct = deductAmount == null ? BigDecimal.ZERO : deductAmount;
        if (deduct.compareTo(deposit) > 0) {
            deduct = deposit;
        }
        record.setDeductAmount(deduct);
        record.setRefundAmount(deposit.subtract(deduct));
        record.setDepositStatus(deduct.compareTo(BigDecimal.ZERO) <= 0 ? 2 : (deduct.compareTo(deposit) >= 0 ? 3 : 4));
        depositRecordService.updateById(record);

        List<Payment> list = list(Wrappers.<Payment>lambdaQuery()
                .eq(Payment::getOrderId, orderId).eq(Payment::getPayType, 1).eq(Payment::getStatus, 1));
        for (Payment pay : list) {
            pay.setStatus(2);
            pay.setRefundTime(LocalDateTime.now());
            updateById(pay);
        }
    }
}
