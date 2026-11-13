package com.camp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.camp.dto.OrderCreateDTO;
import com.camp.entity.Order;
import com.camp.vo.OrderVO;

/**
 * 租赁订单服务接口
 *
 * @author camp
 */
public interface OrderService extends IService<Order> {

    /**
     * 下单（占用库存，订单状态为待支付）
     *
     * @return 订单ID
     */
    Long createOrder(OrderCreateDTO dto);

    /**
     * 模拟支付（待支付 -> 租赁中）
     */
    void payOrder(Long orderId);

    /**
     * 发起归还（租赁中 -> 待归还）
     */
    void applyReturn(Long orderId);

    /**
     * 归还确认（待归还 -> 已完成，恢复库存）
     */
    void confirmReturn(Long orderId);

    /**
     * 分页查询订单
     *
     * @param userId      用户ID（用户端传，管理端传 null）
     * @param orderStatus 订单状态（可空）
     * @param storeId     网点ID（管理端筛选，可空）
     */
    IPage<OrderVO> pageOrders(long current, long size, Long userId, Integer orderStatus, Long storeId);

    /**
     * 订单详情
     */
    OrderVO getOrderDetail(Long orderId);

    /**
     * 标记逾期
     */
    void markOverdue(Long orderId);

    /**
     * 管理端核验归还（-> 已完成，恢复库存）
     */
    void verifyReturn(Long orderId);

    /**
     * 修改订单备注
     */
    void updateRemark(Long orderId, String remark);

    /**
     * 食品下单（一次性售卖：直接完成、无押金、扣减该网点库存）
     *
     * @param dto 食品下单参数
     * @return 生成的订单ID列表
     */
    java.util.List<Long> createFoodOrder(com.camp.dto.FoodOrderDTO dto);
}
