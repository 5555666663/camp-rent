package com.camp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.mapper.StatsMapper;
import com.camp.service.StatsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务实现
 *
 * @author camp
 */
@Service
public class StatsServiceImpl implements StatsService {

    @Resource
    private StatsMapper statsMapper;

    /** 按网点统计订单数量 */
    @Override
    public List<Map<String, Object>> countOrderByStore() {
        return statsMapper.countOrderByStore();
    }

    /** 统计装备租赁频次 */
    @Override
    public List<Map<String, Object>> countEquipmentRent() {
        return statsMapper.countEquipmentRent();
    }

    /** 订单趋势（近30天） */
    @Override
    public List<Map<String, Object>> countOrderTrend() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        return statsMapper.countOrderTrend(start);
    }
}
