package com.camp.vo;

import lombok.Data;

import java.util.List;

/**
 * 网点详情 VO（网点信息 + 该网点可租装备 + 该网点可售食品）
 *
 * @author camp
 */
@Data
public class StoreDetailVO {

    /** 网点信息（含距离） */
    private StoreVO store;

    /** 该网点的装备（租赁） */
    private List<EquipmentVO> equipments;

    /** 该网点的食品（售卖） */
    private List<EquipmentVO> foods;
}
