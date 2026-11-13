package com.camp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.camp.common.Result;
import com.camp.entity.Category;
import com.camp.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 装备分类接口（前台公开浏览）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 获取所有分类（按 sort 升序，用于分类筛选）
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        List<Category> list = categoryService.list(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
        return Result.success(list);
    }
}
