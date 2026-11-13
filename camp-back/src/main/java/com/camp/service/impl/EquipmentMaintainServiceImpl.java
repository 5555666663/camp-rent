package com.camp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.entity.EquipmentMaintain;
import com.camp.mapper.EquipmentMaintainMapper;
import com.camp.service.EquipmentMaintainService;
import com.camp.vo.MaintainVO;
import org.springframework.stereotype.Service;

/**
 * 装备维保服务实现
 *
 * @author camp
 */
@Service
public class EquipmentMaintainServiceImpl extends ServiceImpl<EquipmentMaintainMapper, EquipmentMaintain> implements EquipmentMaintainService {

    /**
     * 分页查询维保记录
     */
    @Override
    public IPage<MaintainVO> pageMaintains(long current, long size, Long storeId, Long equipmentId) {
        Page<MaintainVO> page = new Page<>(current, size);
        return baseMapper.selectMaintainPage(page, storeId, equipmentId);
    }
}
