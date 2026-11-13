package com.camp.vo;

import com.camp.entity.Store;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 网点 VO（带距离）
 *
 * @author camp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StoreVO extends Store {

    /** 距离用户当前位置的距离（公里） */
    private Double distanceKm;
}
