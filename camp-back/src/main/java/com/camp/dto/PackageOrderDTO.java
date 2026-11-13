package com.camp.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 套餐下单请求 DTO
 * <p>既可用于「一键预订整个套餐」，也可用于「自己组合套餐」。</p>
 *
 * @author camp
 */
@Data
public class PackageOrderDTO implements Serializable {

    /** 取件网点ID（套餐内装备需在该网点有库存） */
    @NotNull(message = "请选择取件网点")
    private Long storeId;

    /** 归还网点ID */
    @NotNull(message = "请选择归还网点")
    private Long returnStoreId;

    /** 租赁开始时间 */
    @NotNull(message = "请选择租赁开始时间")
    private LocalDateTime rentStartTime;

    /** 租赁结束时间 */
    @NotNull(message = "请选择租赁结束时间")
    private LocalDateTime rentEndTime;

    /** 自定义组合的装备清单（为空时使用套餐自带清单） */
    private List<Item> items;

    /**
     * 自定义组合的单个装备项
     */
    @Data
    public static class Item implements Serializable {

        /** 装备ID */
        private Long equipmentId;

        /** 数量 */
        private Integer quantity;
    }
}
