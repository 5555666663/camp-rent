package com.camp.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 套餐装备明细 VO
 * <p>套餐明细 + 装备信息联查结果，供前端展示套餐内装备。</p>
 *
 * @author camp
 */
@Data
public class PackageItemVO implements Serializable {

    /** 套餐ID */
    private Long packageId;

    /** 装备ID */
    private Long equipmentId;

    /** 装备数量 */
    private Integer quantity;

    /** 装备名称 */
    private String eqName;

    /** 装备图片 */
    private String eqImg;

    /** 日租金 */
    private BigDecimal pricePerDay;

    /** 押金 */
    private BigDecimal deposit;

    /** 库存 */
    private Integer stockNum;

    /** 装备所属网点ID */
    private Long storeId;
}
