package com.camp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.dto.FoodOrderDTO;
import com.camp.dto.OrderCreateDTO;
import com.camp.service.OrderService;
import com.camp.util.UserContext;
import com.camp.vo.OrderVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 租赁订单接口（用户端，需登录）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 下单（占用库存，生成订单）
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success("下单成功", orderService.createOrder(dto));
    }

    /**
     * 食品下单（一次性售卖，无押金、无归还，直接完成并扣减自提网点库存）
     */
    @PostMapping("/food")
    public Result<List<Long>> createFood(@Valid @RequestBody FoodOrderDTO dto) {
        return Result.success("购买成功", orderService.createFoodOrder(dto));
    }

    /**
     * 模拟支付（待支付 -> 租赁中）
     */
    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.payOrder(id);
        return Result.success("支付成功", null);
    }

    /**
     * 发起归还（租赁中 -> 待归还）
     */
    @PostMapping("/{id}/return")
    public Result<Void> applyReturn(@PathVariable Long id) {
        orderService.applyReturn(id);
        return Result.success("归还申请已提交", null);
    }

    /**
     * 归还确认（待归还 -> 已完成，恢复库存）
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmReturn(@PathVariable Long id) {
        orderService.confirmReturn(id);
        return Result.success("归还完成", null);
    }

    /**
     * 我的订单分页（支持按状态筛选）
     */
    @GetMapping("/page")
    public Result<PageResult<OrderVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer orderStatus) {
        Long userId = UserContext.getUserId();
        IPage<OrderVO> page = orderService.pageOrders(current, size, userId, orderStatus, null);
        return Result.success(PageResult.of(page));
    }

    /**
     * 订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }
}
