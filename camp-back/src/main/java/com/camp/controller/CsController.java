package com.camp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.entity.CsMessage;
import com.camp.entity.Order;
import com.camp.service.CsMessageService;
import com.camp.service.CommentService;
import com.camp.service.OrderService;
import com.camp.vo.CommentVO;
import com.camp.vo.OrderVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 客服工作台接口（ROLE_CS / ROLE_ADMIN 可访问）
 * 包含：全部订单查看与状态处理、售后（评价）处理、咨询消息查看与回复
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/cs")
public class CsController {

    @Resource
    private OrderService orderService;

    @Resource
    private CommentService commentService;

    @Resource
    private CsMessageService csMessageService;

    /**
     * 全部订单分页（可按订单状态、网点筛选）
     */
    @GetMapping("/order/page")
    public Result<PageResult<OrderVO>> orderPage(@RequestParam(defaultValue = "1") long current,
                                                 @RequestParam(defaultValue = "10") long size,
                                                 @RequestParam(required = false) Integer orderStatus,
                                                 @RequestParam(required = false) Long storeId) {
        return Result.success(PageResult.of(orderService.pageOrders(current, size, null, orderStatus, storeId)));
    }

    /**
     * 核验归还（订单流转为已完成并恢复库存）
     */
    @PostMapping("/order/{id}/verify")
    public Result<Void> verifyReturn(@PathVariable Long id) {
        orderService.verifyReturn(id);
        return Result.success();
    }

    /**
     * 标记逾期订单
     */
    @PostMapping("/order/{id}/overdue")
    public Result<Void> markOverdue(@PathVariable Long id) {
        orderService.markOverdue(id);
        return Result.success();
    }

    /**
     * 修改订单状态（客服可人工调整）
     */
    @PutMapping("/order/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestParam Integer orderStatus) {
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        order.setOrderStatus(orderStatus);
        orderService.updateById(order);
        return Result.success();
    }

    /**
     * 售后处理：分页查看用户评价
     */
    @GetMapping("/comment/page")
    public Result<PageResult<CommentVO>> commentPage(@RequestParam(defaultValue = "1") long current,
                                                    @RequestParam(defaultValue = "10") long size,
                                                    @RequestParam(required = false) Long storeId) {
        return Result.success(PageResult.of(commentService.pageComments(current, size, storeId)));
    }

    /**
     * 售后处理：删除不良评价
     */
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.removeById(id);
        return Result.success();
    }

    /**
     * 咨询消息分页（status：0-待处理，1-已回复）
     */
    @GetMapping("/message/page")
    public Result<PageResult<CsMessage>> messagePage(@RequestParam(defaultValue = "1") long current,
                                                     @RequestParam(defaultValue = "10") long size,
                                                     @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<CsMessage> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(CsMessage::getStatus, status);
        }
        wrapper.orderByDesc(CsMessage::getId);
        return Result.success(PageResult.of(csMessageService.page(new Page<>(current, size), wrapper)));
    }

    /**
     * 回复咨询消息
     */
    @PutMapping("/message/{id}/reply")
    public Result<Void> replyMessage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        CsMessage message = csMessageService.getById(id);
        if (message == null) {
            return Result.error("消息不存在");
        }
        message.setReply(body.get("reply"));
        message.setStatus(1);
        csMessageService.updateById(message);
        return Result.success();
    }
}
