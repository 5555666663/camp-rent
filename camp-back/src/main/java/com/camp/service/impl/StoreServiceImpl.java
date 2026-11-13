package com.camp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.entity.Category;
import com.camp.entity.Equipment;
import com.camp.entity.Store;
import com.camp.mapper.StoreMapper;
import com.camp.service.CategoryService;
import com.camp.service.EquipmentService;
import com.camp.service.StoreService;
import com.camp.util.GeoUtil;
import com.camp.vo.EquipmentVO;
import com.camp.vo.StoreDetailVO;
import com.camp.vo.StoreVO;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网点服务实现
 *
 * @author camp
 */
@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements StoreService {

    @Resource
    private EquipmentService equipmentService;

    @Resource
    private CategoryService categoryService;

    /**
     * 查询距离用户最近的网点列表（按距离升序）
     */
    @Override
    public List<StoreVO> nearestStores(BigDecimal longitude, BigDecimal latitude) {
        // 只取启用中的网点
        List<Store> stores = list(new LambdaQueryWrapper<Store>().eq(Store::getStatus, 1));
        List<StoreVO> result = new ArrayList<>();
        for (Store store : stores) {
            StoreVO vo = new StoreVO();
            BeanUtil.copyProperties(store, vo);
            vo.setDistanceKm(GeoUtil.distanceKm(longitude, latitude, store.getLongitude(), store.getLatitude()));
            result.add(vo);
        }
        // 有距离的按升序排在前，无距离的排最后
        result.sort(Comparator.comparing(StoreVO::getDistanceKm, Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    /**
     * 查询网点详情（含距离、该网点装备与食品库存）
     */
    @Override
    public StoreDetailVO storeDetail(Long storeId, BigDecimal longitude, BigDecimal latitude) {
        Store store = getById(storeId);
        if (store == null) {
            return null;
        }
        StoreVO storeVO = new StoreVO();
        BeanUtil.copyProperties(store, storeVO);
        storeVO.setDistanceKm(GeoUtil.distanceKm(longitude, latitude, store.getLongitude(), store.getLatitude()));

        // 分类名称映射（用于回填 categoryName）
        Map<Long, String> categoryNameMap = new HashMap<>();
        for (Category category : categoryService.list()) {
            categoryNameMap.put(category.getId(), category.getCategoryName());
        }

        List<Equipment> all = equipmentService.list(new LambdaQueryWrapper<Equipment>()
                .eq(Equipment::getStoreId, storeId)
                .eq(Equipment::getStatus, 1)
                .orderByAsc(Equipment::getId));

        List<EquipmentVO> equipments = new ArrayList<>();
        List<EquipmentVO> foods = new ArrayList<>();
        for (Equipment eq : all) {
            EquipmentVO vo = new EquipmentVO();
            BeanUtil.copyProperties(eq, vo);
            vo.setStoreName(store.getStoreName());
            vo.setCategoryName(categoryNameMap.get(eq.getCategoryId()));
            if (eq.getProductType() != null && eq.getProductType() == 2) {
                foods.add(vo);
            } else {
                equipments.add(vo);
            }
        }

        StoreDetailVO detailVO = new StoreDetailVO();
        detailVO.setStore(storeVO);
        detailVO.setEquipments(equipments);
        detailVO.setFoods(foods);
        return detailVO;
    }
}
