package com.camp.controller.storage;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.camp.annotation.OperLog;
import com.camp.exception.BusinessException;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.entity.Equipment;
import com.camp.entity.EquipmentMaintain;
import com.camp.entity.FoodBatch;
import com.camp.service.EquipmentMaintainService;
import com.camp.service.EquipmentService;
import com.camp.service.FoodBatchService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 仓储工作台：入库、批次、维保（ROLE_STORAGE / ROLE_ADMIN）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/storage")
public class StorageController {

    @Resource
    private EquipmentService equipmentService;
    @Resource
    private FoodBatchService foodBatchService;
    @Resource
    private EquipmentMaintainService maintainService;

    /** 商品分页（含食品批次提示） */
    @GetMapping("/equipment/page")
    public Result<PageResult<Equipment>> page(@RequestParam(defaultValue = "1") long current,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) String keyword) {
        IPage<Equipment> page = equipmentService.page(new Page<>(current, size),
                Wrappers.<Equipment>lambdaQuery()
                        .like(keyword != null && !keyword.isEmpty(), Equipment::getEqName, keyword)
                        .orderByDesc(Equipment::getId));
        return Result.success(PageResult.of(page));
    }

    /** 入库：装备加库存；食品同时生成批次 */
    @OperLog(module = "仓储管理", action = "商品入库")
    @PostMapping("/inbound")
    public Result<String> inbound(@RequestBody Map<String, Object> body) {
        Long equipmentId = Long.valueOf(String.valueOf(body.get("equipmentId")));
        int quantity = Integer.parseInt(String.valueOf(body.getOrDefault("quantity", 1)));
        if (quantity <= 0) {
            throw new BusinessException("入库数量必须大于 0");
        }
        Equipment eq = equipmentService.getById(equipmentId);
        if (eq == null) {
            throw new BusinessException("商品不存在");
        }
        eq.setStockNum((eq.getStockNum() == null ? 0 : eq.getStockNum()) + quantity);
        equipmentService.updateById(eq);

        if (eq.getProductType() != null && eq.getProductType() == 2) {
            FoodBatch batch = new FoodBatch();
            batch.setEquipmentId(equipmentId);
            batch.setStoreId(eq.getStoreId());
            batch.setBatchNo("B" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + IdUtil.fastSimpleUUID().substring(0, 6));
            batch.setProductionDate(LocalDate.now());
            int days = eq.getShelfLifeDays() == null ? 90 : eq.getShelfLifeDays();
            batch.setExpireDate(LocalDate.now().plusDays(days));
            batch.setQuantity(quantity);
            batch.setStatus(0);
            batch.setCreateTime(LocalDateTime.now());
            foodBatchService.save(batch);
        }
        return Result.success("入库成功");
    }

    /** 食品批次分页 */
    @GetMapping("/batch/page")
    public Result<PageResult<FoodBatch>> batchPage(@RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam(required = false) Long equipmentId) {
        IPage<FoodBatch> page = foodBatchService.page(new Page<>(current, size),
                Wrappers.<FoodBatch>lambdaQuery()
                        .eq(equipmentId != null, FoodBatch::getEquipmentId, equipmentId)
                        .orderByAsc(FoodBatch::getExpireDate));
        return Result.success(PageResult.of(page));
    }

    /** 新增维保记录 */
    @OperLog(module = "仓储管理", action = "新增维保")
    @PostMapping("/maintain")
    public Result<String> maintain(@RequestBody EquipmentMaintain entity) {
        Equipment eq = equipmentService.getById(entity.getEquipmentId());
        if (eq == null) {
            throw new BusinessException("装备不存在");
        }
        if (entity.getStoreId() == null) {
            entity.setStoreId(eq.getStoreId());
        }
        entity.setId(null);
        if (entity.getMaintainTime() == null) {
            entity.setMaintainTime(LocalDateTime.now());
        }
        maintainService.save(entity);
        return Result.success("维保记录已保存");
    }
}
