package com.camp.vo;

import com.camp.entity.Order;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单展示对象（附带网点、用户、装备名称）
 *
 * @author camp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Order {

    /** 取件网点名称 */
    private String pickStoreName;

    /** 归还网点名称 */
    private String returnStoreName;

    /** 下单用户名称 */
    private String username;

    /** 装备名称 */
    private String equipmentName;

    /** 装备图片 */
    private String eqImg;
}
