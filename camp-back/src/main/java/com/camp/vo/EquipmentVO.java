package com.camp.vo;

import com.camp.entity.Equipment;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 装备展示对象（附带网点、分类名称）
 *
 * @author camp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EquipmentVO extends Equipment {

    /** 所属网点名称 */
    private String storeName;

    /** 所属分类名称 */
    private String categoryName;
}
