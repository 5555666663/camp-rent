package com.camp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.dto.FoodOrderDTO;
import com.camp.dto.OrderCreateDTO;
import com.camp.entity.DepositRecord;
import com.camp.entity.Equipment;
import com.camp.entity.Order;
import com.camp.entity.Store;
import com.camp.entity.User;
import com.camp.exception.BusinessException;
import com.camp.mapper.OrderMapper;
import com.camp.service.DepositRecordService;
import com.camp.service.EquipmentService;
import com.camp.service.OrderService;
import com.camp.service.StoreService;
import com.camp.service.UserService;
import com.camp.util.UserContext;
import com.camp.vo.OrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 租赁订单服务实现
 *
 * @author camp
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private EquipmentService equipmentService;

    @Resource
    private StoreService storeService;

    @Resource
    private UserService userService;

    @Resource
    private DepositRecordService depositRecordService;

    /**
     * 下单：校验库存、扣减库存、生成订单与押金记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        // 装备校验
        Equipment equipment = equipmentService.getById(dto.getEquipmentId());
        if (equipment == null || equipment.getStatus() == null || equipment.getStatus() != 1) {
            throw new BusinessException("装备不存在或已下架");
        }
        // 库存归属校验：取件网点必须等于装备所属网点（网点库存独立隔离）
        if (!equipment.getStoreId().equals(dto.getPickStoreId())) {
            throw new BusinessException("该装备库存归属其所在网点，请选择正确的取件网点");
        }
        // 归还网点校验
        Store returnStore = storeService.getById(dto.getReturnStoreId());
        if (returnStore == null || returnStore.getStatus() == null || returnStore.getStatus() != 1) {
            throw new BusinessException("归还网点不存在或已停用");
        }
        // 时间校验：归还时间不能早于租赁开始时间
        if (dto.getRentStartTime() == null || dto.getRentEndTime() == null
                || !dto.getRentEndTime().isAfter(dto.getRentStartTime())) {
            throw new BusinessException("归还时间不能早于租赁开始时间");
        }
        // 计算租赁天数（不足一天按一天算）
        long days = Duration.between(dto.getRentStartTime(), dto.getRentEndTime()).toDays();
        if (days < 1) {
            days = 1;
        }
        // 计算总租金与押金
        BigDecimal totalPrice = equipment.getPricePerDay()
                .multiply(BigDecimal.valueOf(days))
                .multiply(BigDecimal.valueOf(dto.getQuantity()));
        BigDecimal deposit = equipment.getDeposit().multiply(BigDecimal.valueOf(dto.getQuantity()));
        // 扣减取件网点库存（原子操作，不足则失败）
        boolean deducted = equipmentService.deductStock(equipment.getId(), dto.getQuantity());
        if (!deducted) {
            throw new BusinessException("库存不足，无法下单");
        }
        // 生成订单
        Order order = new Order();
        order.setUserId(userId);
        order.setEquipmentId(equipment.getId());
        order.setQuantity(dto.getQuantity());
        order.setPickStoreId(dto.getPickStoreId());
        order.setReturnStoreId(dto.getReturnStoreId());
        order.setRentStartTime(dto.getRentStartTime());
        order.setRentEndTime(dto.getRentEndTime());
        order.setTotalPrice(totalPrice);
        order.setDepositMoney(deposit);
        order.setOrderStatus(0);
        order.setCreateTime(LocalDateTime.now());
        save(order);
        // 生成押金记录（未支付）
        DepositRecord record = new DepositRecord();
        record.setOrderId(order.getId());
        record.setDepositMoney(deposit);
        record.setDepositStatus(0);
        record.setRemark("下单待支付押金");
        record.setCreateTime(LocalDateTime.now());
        depositRecordService.save(record);
        return order.getId();
    }

    /**
     * 模拟支付（待支付 -> 租赁中）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() == null || order.getOrderStatus() != 0) {
            throw new BusinessException("订单状态不允许支付");
        }
        order.setOrderStatus(1);
        updateById(order);
        updateDepositStatus(orderId, 1, "押金已支付");
    }

    /**
     * 发起归还（租赁中 -> 待归还）
     */
    @Override
    public void applyReturn(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() == null || order.getOrderStatus() != 1) {
            throw new BusinessException("订单状态不允许发起归还");
        }
        order.setOrderStatus(2);
        updateById(order);
    }

    /**
     * 归还确认（待归还 -> 已完成，恢复库存、退还押金）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReturn(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() == null || order.getOrderStatus() != 2) {
            throw new BusinessException("订单状态不允许归还确认");
        }
        order.setOrderStatus(3);
        updateById(order);
        // 恢复装备所属网点库存
        equipmentService.restoreStock(order.getEquipmentId(), order.getQuantity());
        updateDepositStatus(orderId, 2, "归还完成，押金已退还");
    }

    /**
     * 分页查询订单
     */
    @Override
    public IPage<OrderVO> pageOrders(long current, long size, Long userId, Integer orderStatus, Long storeId) {
        Page<OrderVO> page = new Page<>(current, size);
        return orderMapper.selectOrderPage(page, userId, orderStatus, storeId);
    }

    /**
     * 订单详情（手动拼装关联信息）
     */
    @Override
    public OrderVO getOrderDetail(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        OrderVO vo = new OrderVO();
        BeanUtil.copyProperties(order, vo);
        Store pickStore = storeService.getById(order.getPickStoreId());
        Store returnStore = storeService.getById(order.getReturnStoreId());
        vo.setPickStoreName(pickStore != null ? pickStore.getStoreName() : null);
        vo.setReturnStoreName(returnStore != null ? returnStore.getStoreName() : null);
        User user = userService.getById(order.getUserId());
        vo.setUsername(user != null ? user.getUsername() : null);
        Equipment equipment = equipmentService.getById(order.getEquipmentId());
        if (equipment != null) {
            vo.setEquipmentName(equipment.getEqName());
            vo.setEqImg(equipment.getEqImg());
        }
        return vo;
    }

    /**
     * 标记逾期
     */
    @Override
    public void markOverdue(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setOrderStatus(4);
        updateById(order);
    }

    /**
     * 管理端核验归还（-> 已完成，恢复库存、退还押金）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyReturn(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() == 3) {
            throw new BusinessException("订单已完成");
        }
        order.setOrderStatus(3);
        updateById(order);
        equipmentService.restoreStock(order.getEquipmentId(), order.getQuantity());
        updateDepositStatus(orderId, 2, "管理端核验归还，押金已退还");
    }

    /**
     * 修改订单备注
     */
    @Override
    public void updateRemark(Long orderId, String remark) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setRemark(remark);
        updateById(order);
    }

    /**
     * 更新押金记录状态
     */
    private void updateDepositStatus(Long orderId, int status, String remark) {
        DepositRecord record = depositRecordService.getOne(
                new LambdaQueryWrapper<DepositRecord>().eq(DepositRecord::getOrderId, orderId));
        if (record != null) {
            record.setDepositStatus(status);
            record.setRemark(remark);
            depositRecordService.updateById(record);
        }
    }

    /**
     * 食品下单（一次性售卖：直接完成、无押金、扣减该网点库存）
     * <p>
     * 食品不支持归还，因此订单直接以「已完成」状态入库；库存扣减发生在自提网点。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> createFoodOrder(FoodOrderDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        Store store = storeService.getById(dto.getStoreId());
        if (store == null || store.getStatus() == null || store.getStatus() != 1) {
            throw new BusinessException("自提网点不存在或已停用");
        }

        List<Long> orderIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (FoodOrderDTO.Item item : dto.getItems()) {
            Equipment food = equipmentService.getById(item.getEquipmentId());
            if (food == null || food.getStatus() == null || food.getStatus() != 1) {
                throw new BusinessException("食品不存在或已下架");
            }
            if (food.getProductType() == null || food.getProductType() != 2) {
                throw new BusinessException("【" + food.getEqName() + "】不是食品，请通过装备租赁下单");
            }
            if (!ObjectUtil.equals(food.getStoreId(), dto.getStoreId())) {
                throw new BusinessException("【" + food.getEqName() + "】不属于该自提网点，库存不互通");
            }
            BigDecimal salePrice = food.getSalePrice() == null ? BigDecimal.ZERO : food.getSalePrice();
            BigDecimal total = salePrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            // 扣减库存（库存不足自动回滚）
            boolean ok = equipmentService.deductStock(food.getId(), item.getQuantity());
            if (!ok) {
                throw new BusinessException("【" + food.getEqName() + "】库存不足");
            }

            Order order = new Order();
            order.setUserId(userId);
            order.setEquipmentId(food.getId());
            order.setQuantity(item.getQuantity());
            order.setPickStoreId(dto.getStoreId());
            order.setReturnStoreId(dto.getStoreId());
            order.setRentStartTime(now);
            order.setRentEndTime(now);
            order.setTotalPrice(total);
            order.setDepositMoney(BigDecimal.ZERO);
            // 食品一次性售卖，无租赁流转，直接完成
            order.setOrderStatus(3);
            order.setRemark("食品售卖订单（一次性）");
            order.setCreateTime(now);
            save(order);
            orderIds.add(order.getId());
        }
        return orderIds;
    }
}
