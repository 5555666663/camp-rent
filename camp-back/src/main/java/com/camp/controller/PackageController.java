package com.camp.controller;

import com.camp.common.Result;
import com.camp.dto.PackageOrderDTO;
import com.camp.service.CampPackageService;
import com.camp.vo.PackageVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 套餐接口
 * <p>套餐浏览为公开接口；套餐下单需要登录。</p>
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/package")
public class PackageController {

    @Resource
    private CampPackageService campPackageService;

    /**
     * 查询在架套餐列表（可按场景筛选）
     *
     * @param sceneType 场景类型：露营 / 徒步，可不传
     * @return 套餐列表
     */
    @GetMapping("/list")
    public Result<List<PackageVO>> list(@RequestParam(required = false) String sceneType) {
        return Result.success(campPackageService.listPackages(sceneType));
    }

    /**
     * 查询套餐详情
     *
     * @param id 套餐ID
     * @return 套餐详情（含装备明细）
     */
    @GetMapping("/{id}")
    public Result<PackageVO> detail(@PathVariable Long id) {
        PackageVO vo = campPackageService.getPackageDetail(id);
        if (vo == null) {
            return Result.error("套餐不存在");
        }
        return Result.success(vo);
    }

    /**
     * 一键预订整个套餐（套餐内每件装备各生成一条订单）
     *
     * @param id  套餐ID
     * @param dto 下单参数
     * @return 生成的订单ID列表
     */
    @PostMapping("/{id}/order")
    public Result<List<Long>> order(@PathVariable Long id, @Valid @RequestBody PackageOrderDTO dto) {
        return Result.success(campPackageService.orderPackage(id, dto));
    }

    /**
     * 自己组合套餐下单（按用户勾选的装备清单生成订单）
     *
     * @param dto 下单参数（含自定义装备清单）
     * @return 生成的订单ID列表
     */
    @PostMapping("/order-custom")
    public Result<List<Long>> orderCustom(@Valid @RequestBody PackageOrderDTO dto) {
        return Result.success(campPackageService.orderCustom(dto));
    }
}
