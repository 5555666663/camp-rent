package com.camp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.exception.BusinessException;
import com.camp.dto.DamageDTO;
import com.camp.entity.DamageRecord;
import com.camp.entity.DepositRecord;
import com.camp.entity.Equipment;
import com.camp.entity.Order;
import com.camp.mapper.DamageRecordMapper;
import com.camp.security.LoginUser;
import com.camp.service.DamageService;
import com.camp.service.DepositRecordService;
import com.camp.service.EquipmentService;
import com.camp.service.OrderService;
import com.camp.service.PayService;
import com.camp.util.UserContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 归还定损服务实现
 *
 * @author camp
 */
@Service
public class DamageServiceImpl extends ServiceImpl<DamageRecordMapper, DamageRecord> implements DamageService {

    @Resource
    private OrderService orderService;
    @Resource
    private EquipmentService equipmentService;
    @Resource
    private DepositRecordService depositRecordService;
    @Resource
    private PayService payService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long orderId, DamageDTO dto) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() == null || (order.getOrderStatus() != 1 && order.getOrderStatus() != 2 && order.getOrderStatus() != 4)) {
            throw new BusinessException("当前订单状态不允许定损");
        }
        String[] imgs = dto.getImgUrls().split(",");
        if (imgs.length > 9) {
            throw new BusinessException("定损照片最多 9 张");
        }
        DepositRecord record = depositRecordService.getOne(
                Wrappers.<DepositRecord>lambdaQuery().eq(DepositRecord::getOrderId, orderId), false);
        BigDecimal deposit = record == null || record.getDepositMoney() == null ? BigDecimal.ZERO : record.getDepositMoney();
        BigDecimal compensate = dto.getCompensateAmount() == null ? BigDecimal.ZERO : dto.getCompensateAmount();
        if (compensate.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("赔偿金额不能为负数");
        }
        if (compensate.compareTo(deposit) > 0) {
            throw new BusinessException("赔偿金额不能超过押金 " + deposit + " 元");
        }

        DamageRecord dr = new DamageRecord();
        dr.setOrderId(orderId);
        dr.setEquipmentId(order.getEquipmentId());
        dr.setImgUrls(dto.getImgUrls());
        dr.setDamageLevel(dto.getDamageLevel());
        dr.setCompensateAmount(compensate);
        dr.setRemark(dto.getRemark());
        dr.setOperator(currentUsername());
        dr.setCreateTime(LocalDateTime.now());
        save(dr);

        // 归还库存
        if (order.getEquipmentId() != null && order.getQuantity() != null) {
            equipmentService.restoreStock(order.getEquipmentId(), order.getQuantity());
        }
        // 押金退款（扣除赔偿）
        payService.refundDeposit(orderId, compensate);

        order.setOrderStatus(3);
        order.setCheckoutTime(LocalDateTime.now());
        orderService.updateById(order);
    }

    private String currentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser) {
            return ((LoginUser) principal).getUsername();
        }
        return "system";
    }

    @Override
    public List<DamageRecord> listByOrder(Long orderId) {
        return list(Wrappers.<DamageRecord>lambdaQuery()
                .eq(DamageRecord::getOrderId, orderId).orderByDesc(DamageRecord::getId));
    }
}
