package com.camp.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 食品下单 DTO（一次性售卖，无租赁时间、无押金）
 *
 * @author camp
 */
@Data
public class FoodOrderDTO {

    /** 自提网点ID */
    @NotNull(message = "请选择自提网点")
    private Long storeId;

    /** 食品明细 */
    @NotEmpty(message = "请至少选择一件食品")
    @Valid
    private List<Item> items;

    /**
     * 食品明细项
     */
    @Data
    public static class Item {

        /** 食品ID（equipment.id） */
        @NotNull(message = "食品ID不能为空")
        private Long equipmentId;

        /** 数量 */
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为 1")
        private Integer quantity;
    }
}
