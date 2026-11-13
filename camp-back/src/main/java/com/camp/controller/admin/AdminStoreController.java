package com.camp.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.entity.Store;
import com.camp.exception.BusinessException;
import com.camp.service.EquipmentService;
import com.camp.service.StoreService;
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
 * 网点管理接口（管理员）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/admin/store")
public class AdminStoreController {

    @Resource
    private StoreService storeService;

    @Resource
    private EquipmentService equipmentService;

    /**
     * 全部网点列表（管理端下拉选择）
     */
    @GetMapping("/list")
    public Result<List<Store>> list() {
        return Result.success(storeService.list(
                new LambdaQueryWrapper<Store>().orderByAsc(Store::getId)));
    }

    /**
     * 网点分页列表（支持关键词、城市、状态筛选）
     */
    @GetMapping("/page")
    public Result<PageResult<Store>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Store> qw = new LambdaQueryWrapper<>();
        qw.like(StrUtil.isNotBlank(keyword), Store::getStoreName, keyword);
        qw.eq(StrUtil.isNotBlank(city), Store::getCity, city);
        qw.eq(status != null, Store::getStatus, status);
        qw.orderByDesc(Store::getId);
        Page<Store> page = storeService.page(new Page<>(current, size), qw);
        return Result.success(PageResult.of(page));
    }

    /**
     * 新增网点
     */
    @PostMapping
    public Result<Void> add(@RequestBody Store store) {
        store.setId(null);
        store.setCreateTime(LocalDateTime.now());
        if (store.getStatus() == null) {
            store.setStatus(1);
        }
        storeService.save(store);
        return Result.success("新增成功", null);
    }

    /**
     * 编辑网点
     */
    @PutMapping
    public Result<Void> update(@RequestBody Store store) {
        storeService.updateById(store);
        return Result.success("修改成功", null);
    }

    /**
     * 删除网点（存在关联装备时禁止删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        long count = equipmentService.count(
                new LambdaQueryWrapper<com.camp.entity.Equipment>().eq(com.camp.entity.Equipment::getStoreId, id));
        if (count > 0) {
            throw new BusinessException("该网点下存在装备，无法删除");
        }
        storeService.removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 启用 / 停用网点
     */
    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam Integer status) {
        Store store = new Store();
        store.setId(id);
        store.setStatus(status);
        storeService.updateById(store);
        return Result.success("操作成功", null);
    }
}
