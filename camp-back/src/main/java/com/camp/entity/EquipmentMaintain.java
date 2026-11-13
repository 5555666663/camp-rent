package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 装备维保实体
 *
 * @author camp
 */
@Data
@TableName("equipment_maintain")
public class EquipmentMaintain implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 装备ID */
    private Long equipmentId;

    /** 所属网点ID */
    private Long storeId;

    /** 维保类型：维修/保养/报废 */
    private String maintainType;

    /** 维保内容 */
    private String content;

    /** 维保时间 */
    private LocalDateTime maintainTime;

    /** 操作人 */
    private String operator;

    /** 备注 */
    private String remark;
}
