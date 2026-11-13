package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 露营套餐实体
 * <p>套餐是若干装备的组合，用于「天气 AI 套餐推荐」场景。</p>
 *
 * @author camp
 */
@Data
@TableName("camp_package")
public class CampPackage implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 套餐名称 */
    private String packageName;

    /** 适用场景：露营 / 徒步 */
    private String sceneType;

    /** 套餐封面图 */
    private String coverImg;

    /** 套餐描述 */
    private String description;

    /** 套餐日租价（元/天） */
    private BigDecimal price;

    /** 原价（元/天），用于展示优惠 */
    private BigDecimal originalPrice;

    /** 状态：0-下架，1-上架 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
