package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 归还定损记录
 *
 * @author camp
 */
@Data
@TableName("damage_record")
public class DamageRecord {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 装备ID */
    private Long equipmentId;

    /** 照片地址，逗号分隔（1~9 张） */
    private String imgUrls;

    /** 0无 1轻微 2中度 3严重 */
    private Integer damageLevel;

    /** 赔偿金额 */
    private BigDecimal compensateAmount;

    /** 备注 */
    private String remark;

    /** 操作人 */
    private String operator;

    /** 创建时间 */
    private LocalDateTime createTime;
}
