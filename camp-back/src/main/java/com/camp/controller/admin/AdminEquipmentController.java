package com.camp.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.entity.Equipment;
import com.camp.service.EquipmentService;
import com.camp.vo.EquipmentVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 装备管理接口（管理员）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/admin/equipment")
public class AdminEquipmentController {

    @Resource
    private EquipmentService equipmentService;

    /**
     * 全部装备列表（管理端下拉选择）
     */
    @GetMapping("/list")
    public Result<List<Equipment>> list() {
        return Result.success(equipmentService.list(
                new LambdaQueryWrapper<Equipment>().orderByDesc(Equipment::getId)));
    }

    /**
     * 商品分页列表（支持网点、分类、关键词、状态、商品类型筛选）
     */
    @GetMapping("/page")
    public Result<PageResult<EquipmentVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer productType) {
        IPage<EquipmentVO> page = equipmentService.pageEquipment(current, size, storeId, categoryId,
                keyword, status, productType);
        return Result.success(PageResult.of(page));
    }

    /**
     * 新增装备
     */
    @PostMapping
    public Result<Void> add(@RequestBody Equipment equipment) {
        equipment.setId(null);
        equipment.setCreateTime(LocalDateTime.now());
        if (equipment.getStatus() == null) {
            equipment.setStatus(1);
        }
        equipmentService.save(equipment);
        return Result.success("新增成功", null);
    }

    /**
     * 编辑装备
     */
    @PutMapping
    public Result<Void> update(@RequestBody Equipment equipment) {
        equipmentService.updateById(equipment);
        return Result.success("修改成功", null);
    }

    /**
     * 删除装备
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentService.removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 上架 / 下架装备
     */
    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam Integer status) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setStatus(status);
        equipmentService.updateById(equipment);
        return Result.success("操作成功", null);
    }
}
