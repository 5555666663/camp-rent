package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 食品批次（保质期管理）
 *
 * @author camp
 */
@Data
@TableName("food_batch")
public class FoodBatch {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 食品ID（关联 equipment） */
    private Long equipmentId;

    /** 所属网点 */
    private Long storeId;

    /** 批次号 */
    private String batchNo;

    /** 生产日期 */
    private LocalDate productionDate;

    /** 过期日期 */
    private LocalDate expireDate;

    /** 数量 */
    private Integer quantity;

    /** 0正常 1临期 2过期 3售罄 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
