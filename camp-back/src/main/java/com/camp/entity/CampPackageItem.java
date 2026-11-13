package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 套餐装备明细实体
 * <p>记录一个套餐包含哪些装备、各自数量。</p>
 *
 * @author camp
 */
@Data
@TableName("camp_package_item")
public class CampPackageItem implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 套餐ID */
    private Long packageId;

    /** 装备ID */
    private Long equipmentId;

    /** 装备数量 */
    private Integer quantity;

    /** 创建时间 */
    private LocalDateTime createTime;
}
