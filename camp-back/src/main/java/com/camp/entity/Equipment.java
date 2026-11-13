package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 露营装备实体
 *
 * @author camp
 */
@Data
@TableName("equipment")
public class Equipment implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属网点ID */
    private Long storeId;

    /** 所属分类ID */
    private Long categoryId;

    /** 商品类型：1-装备（租赁），2-食品（一次性售卖） */
    private Integer productType;

    /** 装备名称 */
    private String eqName;

    /** 规格，如 2 人 / 320g/盒 */
    private String spec;

    /** 装备图片地址 */
    private String eqImg;

    /** 日租金（元/天，装备使用） */
    private BigDecimal pricePerDay;

    /** 售卖价格（元，食品使用） */
    private BigDecimal salePrice;

    /** 押金（元） */
    private BigDecimal deposit;

    /** 库存数量 */
    private Integer stockNum;

    /** 装备描述 */
    private String description;

    /** 状态：0-下架，1-上架 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
