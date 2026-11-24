package com.camp.controller;

import com.camp.common.Result;
import com.camp.dto.PayCreateDTO;
import com.camp.service.PayService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 支付接口（沙箱模拟通道）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/pay")
public class PayController {

    @Resource
    private PayService payService;

    /** 创建支付单（押金+租金），返回模拟收银台地址 */
    @PostMapping("/create")
    public Result<Map<String, Object>> create(@Validated @RequestBody PayCreateDTO dto) {
        return Result.success(payService.create(dto));
    }

    /** 沙箱收银台：访问即视为支付成功（模拟第三方回调） */
    @GetMapping("/mock/{payNo}")
    public Result<String> mockPay(@PathVariable String payNo) {
        payService.notifySuccess(payNo, "SANDBOX" + System.currentTimeMillis());
        return Result.success("沙箱支付成功");
    }

    /** 第三方异步回调入口（真实沙箱对接时使用） */
    @PostMapping("/notify")
    public String notify(@RequestParam String payNo,
                         @RequestParam(required = false) String tradeNo,
                         HttpServletRequest request) {
        payService.notifySuccess(payNo, tradeNo);
        return "success";
    }

    /** 查询订单支付单与押金记录 */
    @GetMapping("/{orderId}")
    public Result<Map<String, Object>> detail(@PathVariable Long orderId) {
        return Result.success(payService.detail(orderId));
    }

    /** 管理员手工退款（押金） */
    @PostMapping("/{orderId}/refund")
    public Result<String> refund(@PathVariable Long orderId) {
        payService.refundDeposit(orderId, null);
        return Result.success("退款已提交（沙箱）");
    }
}
