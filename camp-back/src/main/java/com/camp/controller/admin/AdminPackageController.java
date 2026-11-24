package com.camp.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.camp.annotation.OperLog;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.entity.CampPackage;
import com.camp.mapper.CampPackageMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 套餐管理（仅超级管理员）：场景套餐的增删改查与上下架
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/admin/package")
public class AdminPackageController {

    @Resource
    private CampPackageMapper campPackageMapper;

    /** 套餐分页 */
    @GetMapping("/page")
    public Result<PageResult<CampPackage>> page(@RequestParam(defaultValue = "1") long current,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) String sceneType,
                                                @RequestParam(required = false) String keyword) {
        IPage<CampPackage> page = campPackageMapper.selectPage(new Page<>(current, size),
                Wrappers.<CampPackage>lambdaQuery()
                        .eq(sceneType != null && !sceneType.isEmpty(), CampPackage::getSceneType, sceneType)
                        .like(keyword != null && !keyword.isEmpty(), CampPackage::getPackageName, keyword)
                        .orderByDesc(CampPackage::getId));
        return Result.success(PageResult.of(page));
    }

    /** 新增套餐 */
    @OperLog(module = "套餐管理", action = "新增套餐")
    @PostMapping
    public Result<String> add(@RequestBody CampPackage entity) {
        entity.setId(null);
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        entity.setCreateTime(LocalDateTime.now());
        campPackageMapper.insert(entity);
        return Result.success("新增成功");
    }

    /** 编辑套餐 */
    @OperLog(module = "套餐管理", action = "编辑套餐")
    @PutMapping
    public Result<String> update(@RequestBody CampPackage entity) {
        campPackageMapper.updateById(entity);
        return Result.success("保存成功");
    }

    /** 删除套餐 */
    @OperLog(module = "套餐管理", action = "删除套餐")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        campPackageMapper.deleteById(id);
        return Result.success("删除成功");
    }

    /** 上架 / 下架 */
    @OperLog(module = "套餐管理", action = "套餐上下架")
    @PutMapping("/{id}/status")
    public Result<String> status(@PathVariable Long id, @RequestParam Integer status) {
        CampPackage pkg = campPackageMapper.selectById(id);
        if (pkg == null) {
            return Result.error("套餐不存在");
        }
        pkg.setStatus(status);
        campPackageMapper.updateById(pkg);
        return Result.success("操作成功");
    }
}
