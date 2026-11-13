package com.camp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据统计 Mapper（ECharts 可视化数据）
 *
 * @author camp
 */
@Mapper
public interface StatsMapper {

    /**
     * 按网点统计订单数量
     */
    @Select("SELECT s.store_name AS name, COUNT(o.id) AS value " +
            "FROM store s LEFT JOIN orders o ON o.pick_store_id = s.id " +
            "GROUP BY s.id, s.store_name ORDER BY value DESC")
    List<Map<String, Object>> countOrderByStore();

    /**
     * 统计装备租赁频次（按订单数降序，前10）
     */
    @Select("SELECT e.eq_name AS name, COUNT(o.id) AS value " +
            "FROM equipment e LEFT JOIN orders o ON o.equipment_id = e.id " +
            "GROUP BY e.id, e.eq_name ORDER BY value DESC LIMIT 10")
    List<Map<String, Object>> countEquipmentRent();

    /**
     * 订单趋势（按天统计近30天订单量）
     */
    @Select("SELECT DATE_FORMAT(o.create_time, '%Y-%m-%d') AS name, COUNT(*) AS value " +
            "FROM orders o WHERE o.create_time >= #{startTime} " +
            "GROUP BY DATE_FORMAT(o.create_time, '%Y-%m-%d') ORDER BY name")
    List<Map<String, Object>> countOrderTrend(@Param("startTime") LocalDateTime startTime);
}
