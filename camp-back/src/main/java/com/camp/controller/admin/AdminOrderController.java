package com.camp.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.service.OrderService;
import com.camp.vo.OrderVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 订单管理接口（管理员）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    @Resource
    private OrderService orderService;

    /**
     * 全部订单分页列表（支持网点、状态筛选）
     */
    @GetMapping("/page")
    public Result<PageResult<OrderVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Integer orderStatus) {
        IPage<OrderVO> page = orderService.pageOrders(current, size, null, orderStatus, storeId);
        return Result.success(PageResult.of(page));
    }

    /**
     * 标记订单逾期
     */
    @PostMapping("/{id}/overdue")
    public Result<Void> markOverdue(@PathVariable Long id) {
        orderService.markOverdue(id);
        return Result.success("已标记逾期", null);
    }

    /**
     * 核验归还（订单流转为已完成，恢复库存）
     */
    @PostMapping("/{id}/verify")
    public Result<Void> verifyReturn(@PathVariable Long id) {
        orderService.verifyReturn(id);
        return Result.success("核验归还完成", null);
    }

    /**
     * 修改订单备注
     */
    @PutMapping("/{id}/remark")
    public Result<Void> updateRemark(@PathVariable Long id, @RequestBody Map<String, String> body) {
        orderService.updateRemark(id, body.get("remark"));
        return Result.success("修改成功", null);
    }
}
