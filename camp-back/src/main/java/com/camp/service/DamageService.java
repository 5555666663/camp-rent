package com.camp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.camp.dto.DamageDTO;
import com.camp.entity.DamageRecord;

import java.util.List;

/**
 * 归还定损服务
 *
 * @author camp
 */
public interface DamageService extends IService<DamageRecord> {

    /** 提交定损并完成归还（扣减押金、回补库存、订单完成） */
    void submit(Long orderId, DamageDTO dto);

    /** 查询订单定损记录 */
    List<DamageRecord> listByOrder(Long orderId);
}
