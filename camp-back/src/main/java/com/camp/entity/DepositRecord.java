package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 押金记录实体
 *
 * @author camp
 */
@Data
@TableName("deposit_record")
public class DepositRecord implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联订单ID */
    private Long orderId;

    /** 押金金额（元） */
    private BigDecimal depositMoney;

    /** 押金状态：0-未支付,1-已支付,2-已退还,3-已扣除 */
    private Integer depositStatus;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;
}
