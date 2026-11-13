package com.camp.vo;

import com.camp.entity.EquipmentMaintain;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 装备维保展示对象（附带装备、网点名称）
 *
 * @author camp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MaintainVO extends EquipmentMaintain {

    /** 装备名称 */
    private String equipmentName;

    /** 网点名称 */
    private String storeName;
}
