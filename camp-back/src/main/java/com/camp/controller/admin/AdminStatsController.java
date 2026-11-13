package com.camp.controller.admin;

import com.camp.common.Result;
import com.camp.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 数据统计接口（管理员，供 ECharts 可视化使用）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    @Resource
    private StatsService statsService;

    /**
     * 按网点统计订单数量
     */
    @GetMapping("/orderByStore")
    public Result<List<Map<String, Object>>> orderByStore() {
        return Result.success(statsService.countOrderByStore());
    }

    /**
     * 装备租赁频次统计
     */
    @GetMapping("/equipmentRent")
    public Result<List<Map<String, Object>>> equipmentRent() {
        return Result.success(statsService.countEquipmentRent());
    }

    /**
     * 订单趋势（近30天）
     */
    @GetMapping("/orderTrend")
    public Result<List<Map<String, Object>>> orderTrend() {
        return Result.success(statsService.countOrderTrend());
    }
}
