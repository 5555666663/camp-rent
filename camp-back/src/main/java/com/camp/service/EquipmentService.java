package com.camp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.camp.entity.Equipment;
import com.camp.vo.EquipmentVO;

/**
 * 露营装备服务接口
 *
 * @author camp
 */
public interface EquipmentService extends IService<Equipment> {

    /**
     * 分页查询商品列表（装备或食品，附带网点、分类名称）
     *
     * @param productType 商品类型（可空，1-装备 2-食品）
     */
    IPage<EquipmentVO> pageEquipment(long current, long size, Long storeId, Long categoryId,
                                     String keyword, Integer status, Integer productType);

    /**
     * 扣减库存（库存不足返回 false）
     */
    boolean deductStock(Long id, int num);

    /**
     * 恢复库存
     */
    void restoreStock(Long id, int num);
}
