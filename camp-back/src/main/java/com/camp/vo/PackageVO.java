package com.camp.vo;

import com.camp.entity.CampPackage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 套餐 VO
 * <p>套餐实体 + 套餐内装备明细列表。</p>
 *
 * @author camp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PackageVO extends CampPackage {

    /** 套餐内装备明细 */
    private List<PackageItemVO> items;
}
