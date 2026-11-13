package com.camp.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.entity.Category;
import com.camp.entity.Equipment;
import com.camp.exception.BusinessException;
import com.camp.service.CategoryService;
import com.camp.service.EquipmentService;
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

/**
 * 装备分类管理接口（管理员）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/admin/category")
public class AdminCategoryController {

    @Resource
    private CategoryService categoryService;

    @Resource
    private EquipmentService equipmentService;

    /**
     * 分类分页列表
     */
    @GetMapping("/page")
    public Result<PageResult<Category>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        LambdaQueryWrapper<Category> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(Category::getSort);
        Page<Category> page = categoryService.page(new Page<>(current, size), qw);
        return Result.success(PageResult.of(page));
    }

    /**
     * 新增分类
     */
    @PostMapping
    public Result<Void> add(@RequestBody Category category) {
        category.setId(null);
        category.setCreateTime(LocalDateTime.now());
        if (category.getSort() == null) {
            category.setSort(0);
        }
        categoryService.save(category);
        return Result.success("新增成功", null);
    }

    /**
     * 编辑分类（含排序）
     */
    @PutMapping
    public Result<Void> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success("修改成功", null);
    }

    /**
     * 删除分类（存在关联装备时禁止删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        long count = equipmentService.count(
                new LambdaQueryWrapper<Equipment>().eq(Equipment::getCategoryId, id));
        if (count > 0) {
            throw new BusinessException("该分类下存在装备，无法删除");
        }
        categoryService.removeById(id);
        return Result.success("删除成功", null);
    }
}
