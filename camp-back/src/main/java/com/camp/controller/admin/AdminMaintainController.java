package com.camp.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.entity.Equipment;
import com.camp.entity.EquipmentMaintain;
import com.camp.service.EquipmentMaintainService;
import com.camp.service.EquipmentService;
import com.camp.vo.MaintainVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 装备维保管理接口（管理员）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/admin/maintain")
public class AdminMaintainController {

    @Resource
    private EquipmentMaintainService maintainService;

    @Resource
    private EquipmentService equipmentService;

    /**
     * 维保记录分页列表（支持网点、装备筛选）
     */
    @GetMapping("/page")
    public Result<PageResult<MaintainVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long equipmentId) {
        IPage<MaintainVO> page = maintainService.pageMaintains(current, size, storeId, equipmentId);
        return Result.success(PageResult.of(page));
    }

    /**
     * 新增维保记录（未传网点时，自动带出装备所属网点）
     */
    @PostMapping
    public Result<Void> add(@RequestBody EquipmentMaintain maintain) {
        if (maintain.getStoreId() == null && maintain.getEquipmentId() != null) {
            Equipment equipment = equipmentService.getById(maintain.getEquipmentId());
            if (equipment != null) {
                maintain.setStoreId(equipment.getStoreId());
            }
        }
        maintainService.save(maintain);
        return Result.success("新增成功", null);
    }
}
