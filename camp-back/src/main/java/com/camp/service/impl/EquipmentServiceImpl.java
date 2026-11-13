package com.camp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.entity.Equipment;
import com.camp.mapper.EquipmentMapper;
import com.camp.service.EquipmentService;
import com.camp.vo.EquipmentVO;
import org.springframework.stereotype.Service;

/**
 * 露营装备服务实现
 *
 * @author camp
 */
@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {

    /**
     * 分页查询商品列表（装备或食品）
     */
    @Override
    public IPage<EquipmentVO> pageEquipment(long current, long size, Long storeId, Long categoryId,
                                            String keyword, Integer status, Integer productType) {
        Page<EquipmentVO> page = new Page<>(current, size);
        return baseMapper.selectEquipmentPage(page, storeId, categoryId, keyword, status, productType);
    }

    /**
     * 扣减库存（原子操作，库存不足返回 false）
     */
    @Override
    public boolean deductStock(Long id, int num) {
        return baseMapper.deductStock(id, num) > 0;
    }

    /**
     * 恢复库存
     */
    @Override
    public void restoreStock(Long id, int num) {
        baseMapper.restoreStock(id, num);
    }
}
