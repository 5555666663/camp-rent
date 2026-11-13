package com.camp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.camp.entity.EquipmentMaintain;
import com.camp.vo.MaintainVO;

/**
 * 装备维保服务接口
 *
 * @author camp
 */
public interface EquipmentMaintainService extends IService<EquipmentMaintain> {

    /**
     * 分页查询维保记录（附带装备、网点名称）
     */
    IPage<MaintainVO> pageMaintains(long current, long size, Long storeId, Long equipmentId);
}
