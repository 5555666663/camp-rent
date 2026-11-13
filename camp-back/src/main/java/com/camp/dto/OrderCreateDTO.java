package com.camp.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 下单请求参数
 *
 * @author camp
 */
@Data
public class OrderCreateDTO implements Serializable {

    /** 装备ID */
    @NotNull(message = "请选择租赁装备")
    private Long equipmentId;

    /** 租赁数量 */
    @NotNull(message = "租赁数量不能为空")
    @Min(value = 1, message = "租赁数量至少为1")
    private Integer quantity;

    /** 取件网点ID */
    @NotNull(message = "请选择取件网点")
    private Long pickStoreId;

    /** 归还网点ID */
    @NotNull(message = "请选择归还网点")
    private Long returnStoreId;

    /** 租赁开始时间 */
    @NotNull(message = "请选择租赁开始时间")
    private LocalDateTime rentStartTime;

    /** 租赁结束时间 */
    @NotNull(message = "请选择租赁结束时间")
    private LocalDateTime rentEndTime;
}
