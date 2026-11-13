package com.camp.service;

import java.util.List;
import java.util.Map;

/**
 * 数据统计服务接口
 *
 * @author camp
 */
public interface StatsService {

    /**
     * 按网点统计订单数量
     */
    List<Map<String, Object>> countOrderByStore();

    /**
     * 统计装备租赁频次
     */
    List<Map<String, Object>> countEquipmentRent();

    /**
     * 订单趋势（近30天）
     */
    List<Map<String, Object>> countOrderTrend();
}
