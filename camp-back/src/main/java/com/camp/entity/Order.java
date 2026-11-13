package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租赁订单实体
 * 说明：order 为 MySQL 保留字，表名使用 orders
 *
 * @author camp
 */
@Data
@TableName("orders")
public class Order implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 下单用户ID */
    private Long userId;

    /** 租赁装备ID */
    private Long equipmentId;

    /** 租赁数量 */
    private Integer quantity;

    /** 取件网点ID（下单扣减该网点库存） */
    private Long pickStoreId;

    /** 归还网点ID（归还恢复该网点库存） */
    private Long returnStoreId;

    /** 租赁开始时间 */
    private LocalDateTime rentStartTime;

    /** 租赁结束时间 */
    private LocalDateTime rentEndTime;

    /** 总租金（元） */
    private BigDecimal totalPrice;

    /** 押金（元） */
    private BigDecimal depositMoney;

    /** 订单状态：0-待支付,1-租赁中,2-待归还,3-已完成,4-逾期 */
    private Integer orderStatus;

    /** 订单备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;
}
