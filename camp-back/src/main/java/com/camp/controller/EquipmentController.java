package com.camp.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.entity.Category;
import com.camp.entity.Equipment;
import com.camp.entity.Store;
import com.camp.service.CategoryService;
import com.camp.service.EquipmentService;
import com.camp.service.StoreService;
import com.camp.vo.EquipmentVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 露营装备接口（前台公开浏览）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Resource
    private EquipmentService equipmentService;

    @Resource
    private StoreService storeService;

    @Resource
    private CategoryService categoryService;

    /**
     * 分页查询商品列表（支持网点、分类、关键词、状态、商品类型筛选）
     *
     * @param productType 商品类型：1-装备（租赁），2-食品（售卖），不传则查全部
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
     * 装备详情（附带网点、分类名称）
     */
    @GetMapping("/{id}")
    public Result<EquipmentVO> detail(@PathVariable Long id) {
        Equipment equipment = equipmentService.getById(id);
        if (equipment == null) {
            return Result.error("装备不存在");
        }
        EquipmentVO vo = new EquipmentVO();
        BeanUtil.copyProperties(equipment, vo);
        Store store = storeService.getById(equipment.getStoreId());
        Category category = categoryService.getById(equipment.getCategoryId());
        vo.setStoreName(store != null ? store.getStoreName() : null);
        vo.setCategoryName(category != null ? category.getCategoryName() : null);
        return Result.success(vo);
    }
}
