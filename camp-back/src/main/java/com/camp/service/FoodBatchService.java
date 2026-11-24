package com.camp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.camp.entity.FoodBatch;

import java.util.List;

/**
 * 食品批次（保质期）服务
 *
 * @author camp
 */
public interface FoodBatchService extends IService<FoodBatch> {

    /** 可售数量（未过期且状态正常） */
    int availableQuantity(Long equipmentId);

    /** 查询某食品批次（按过期时间升序） */
    List<FoodBatch> listByEquipment(Long equipmentId);

    /** 刷新批次状态（临期/过期/售罄） */
    void refreshStatus();

    /** 后台预警：临期或过期批次 */
    List<FoodBatch> warnList();

    /** 按先进先出扣减批次 */
    void deduct(Long equipmentId, int quantity);
}
