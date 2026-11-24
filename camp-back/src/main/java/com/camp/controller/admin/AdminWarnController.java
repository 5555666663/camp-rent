package com.camp.controller.admin;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.camp.common.Result;
import com.camp.entity.Equipment;
import com.camp.entity.FoodBatch;
import com.camp.service.EquipmentService;
import com.camp.service.FoodBatchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台：库存与保质期预警
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/admin/warn")
public class AdminWarnController {

    @Resource
    private FoodBatchService foodBatchService;
    @Resource
    private EquipmentService equipmentService;

    /** 低库存商品（低于 min_stock） */
    @GetMapping("/stock")
    public Result<List<Map<String, Object>>> stock() {
        List<Equipment> list = equipmentService.list(Wrappers.<Equipment>lambdaQuery()
                .eq(Equipment::getStatus, 1)
                .isNotNull(Equipment::getMinStock));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Equipment e : list) {
            int stock = e.getStockNum() == null ? 0 : e.getStockNum();
            int min = e.getMinStock() == null ? 10 : e.getMinStock();
            if (stock < min) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", e.getId());
                row.put("eqName", e.getEqName());
                row.put("brand", e.getBrand());
                row.put("stockNum", stock);
                row.put("minStock", min);
                row.put("storeId", e.getStoreId());
                result.add(row);
            }
        }
        return Result.success(result);
    }

    /** 临期/过期批次 */
    @GetMapping("/expire")
    public Result<List<FoodBatch>> expire() {
        return Result.success(foodBatchService.warnList());
    }
}
