package com.camp.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.entity.FoodBatch;
import com.camp.mapper.FoodBatchMapper;
import com.camp.service.FoodBatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 食品批次服务实现（保质期 + 先进先出）
 *
 * @author camp
 */
@Service
public class FoodBatchServiceImpl extends ServiceImpl<FoodBatchMapper, FoodBatch> implements FoodBatchService {

    @Override
    public int availableQuantity(Long equipmentId) {
        LocalDate today = LocalDate.now();
        List<FoodBatch> list = list(Wrappers.<FoodBatch>lambdaQuery()
                .eq(FoodBatch::getEquipmentId, equipmentId)
                .eq(FoodBatch::getStatus, 0)
                .ge(FoodBatch::getExpireDate, today));
        return list.stream().mapToInt(b -> b.getQuantity() == null ? 0 : b.getQuantity()).sum();
    }

    @Override
    public List<FoodBatch> listByEquipment(Long equipmentId) {
        return list(Wrappers.<FoodBatch>lambdaQuery()
                .eq(FoodBatch::getEquipmentId, equipmentId)
                .orderByAsc(FoodBatch::getExpireDate));
    }

    @Override
    public void refreshStatus() {
        LocalDate today = LocalDate.now();
        List<FoodBatch> all = list();
        for (FoodBatch b : all) {
            int status = 0;
            if (b.getQuantity() == null || b.getQuantity() <= 0) {
                status = 3;
            } else if (b.getExpireDate() != null && b.getExpireDate().isBefore(today)) {
                status = 2;
            } else if (b.getExpireDate() != null && !b.getExpireDate().isAfter(today.plusDays(7))) {
                status = 1;
            }
            if (!Integer.valueOf(status).equals(b.getStatus())) {
                b.setStatus(status);
                updateById(b);
            }
        }
    }

    @Override
    public List<FoodBatch> warnList() {
        LocalDate today = LocalDate.now();
        return list(Wrappers.<FoodBatch>lambdaQuery()
                .in(FoodBatch::getStatus, 1, 2)
                .or(w -> w.le(FoodBatch::getExpireDate, today.plusDays(7)))
                .orderByAsc(FoodBatch::getExpireDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(Long equipmentId, int quantity) {
        int remain = quantity;
        List<FoodBatch> list = list(Wrappers.<FoodBatch>lambdaQuery()
                .eq(FoodBatch::getEquipmentId, equipmentId)
                .eq(FoodBatch::getStatus, 0)
                .ge(FoodBatch::getExpireDate, LocalDate.now())
                .orderByAsc(FoodBatch::getExpireDate));
        for (FoodBatch b : list) {
            if (remain <= 0) {
                break;
            }
            int q = b.getQuantity() == null ? 0 : b.getQuantity();
            int take = Math.min(q, remain);
            b.setQuantity(q - take);
            if (b.getQuantity() <= 0) {
                b.setStatus(3);
            }
            updateById(b);
            remain -= take;
        }
        if (remain > 0) {
            throw new com.camp.exception.BusinessException("食品可售库存不足（考虑保质期后仅剩 " + (quantity - remain) + " 件）");
        }
    }
}
