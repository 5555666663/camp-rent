package com.camp.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.camp.common.Result;
import com.camp.entity.Store;
import com.camp.service.StoreService;
import com.camp.vo.StoreDetailVO;
import com.camp.vo.StoreVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 网点接口（前台公开浏览）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/store")
public class StoreController {

    @Resource
    private StoreService storeService;

    /**
     * 获取所有启用中的网点（地图 marker、下拉选择）
     */
    @GetMapping("/list")
    public Result<List<Store>> list() {
        List<Store> list = storeService.list(
                new LambdaQueryWrapper<Store>().eq(Store::getStatus, 1).orderByAsc(Store::getId));
        return Result.success(list);
    }

    /**
     * 获取启用网点的城市列表（去重，用于城市筛选下拉）
     */
    @GetMapping("/cities")
    public Result<List<String>> cities() {
        List<Store> stores = storeService.list(
                new LambdaQueryWrapper<Store>().eq(Store::getStatus, 1));
        List<String> cities = stores.stream()
                .map(Store::getCity)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        return Result.success(cities);
    }

    /**
     * 就近网点列表：按用户经纬度计算距离并升序返回
     * （距离计算在后端完成，前端只传经纬度，保护高德 Key）
     */
    @GetMapping("/nearest")
    public Result<List<StoreVO>> nearest(@RequestParam BigDecimal longitude,
                                         @RequestParam BigDecimal latitude) {
        return Result.success(storeService.nearestStores(longitude, latitude));
    }

    /**
     * 网点详情：网点信息 + 距离 + 该网点可租装备 + 该网点可售食品
     */
    @GetMapping("/{id}/detail")
    public Result<StoreDetailVO> detailWithStock(@PathVariable Long id,
                                                 @RequestParam(required = false) BigDecimal longitude,
                                                 @RequestParam(required = false) BigDecimal latitude) {
        StoreDetailVO vo = storeService.storeDetail(id, longitude, latitude);
        if (vo == null) {
            return Result.error("网点不存在");
        }
        return Result.success(vo);
    }

    /**
     * 网点详情（纯网点信息）
     */
    @GetMapping("/{id}")
    public Result<Store> detail(@PathVariable Long id) {
        return Result.success(storeService.getById(id));
    }
}
