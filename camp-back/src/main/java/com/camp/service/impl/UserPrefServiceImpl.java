package com.camp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.camp.entity.Category;
import com.camp.entity.Equipment;
import com.camp.entity.Order;
import com.camp.mapper.CategoryMapper;
import com.camp.mapper.EquipmentMapper;
import com.camp.mapper.OrderMapper;
import com.camp.service.UserPrefService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户租赁偏好服务实现
 *
 * @author camp
 */
@Service
public class UserPrefServiceImpl implements UserPrefService {

    /** 参考最近订单条数 */
    private static final int RECENT = 20;

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private EquipmentMapper equipmentMapper;
    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public String summary(Long userId) {
        if (userId == null) {
            return null;
        }
        List<Order> orders = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getId)
                .last("LIMIT " + RECENT));
        if (orders == null || orders.isEmpty()) {
            return null;
        }
        Map<Long, Integer> eqCount = new HashMap<>();
        for (Order o : orders) {
            if (o.getEquipmentId() == null) {
                continue;
            }
            eqCount.merge(o.getEquipmentId(), 1, Integer::sum);
        }
        if (eqCount.isEmpty()) {
            return null;
        }
        List<Long> eqIds = new ArrayList<>(eqCount.keySet());
        List<Equipment> equipments = equipmentMapper.selectBatchIds(eqIds);
        Map<Long, Integer> catCount = new HashMap<>();
        for (Equipment e : equipments) {
            if (e.getCategoryId() != null) {
                catCount.merge(e.getCategoryId(), eqCount.getOrDefault(e.getId(), 1), Integer::sum);
            }
        }
        if (catCount.isEmpty()) {
            return null;
        }
        List<Map.Entry<Long, Integer>> sorted = new ArrayList<>(catCount.entrySet());
        sorted.sort(Comparator.comparingInt((Map.Entry<Long, Integer> en) -> en.getValue()).reversed());
        List<String> names = new ArrayList<>();
        for (int i = 0; i < sorted.size() && i < 2; i++) {
            Category c = categoryMapper.selectById(sorted.get(i).getKey());
            if (c != null && c.getCategoryName() != null) {
                names.add(c.getCategoryName());
            }
        }
        if (names.isEmpty()) {
            return null;
        }
        return "您近半年常租「" + String.join("、", names) + "」，已按此偏好优先推荐";
    }
}
