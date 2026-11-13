package com.camp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.camp.common.Result;
import com.camp.dto.RecommendDTO;
import com.camp.entity.Equipment;
import com.camp.service.AiRecommendService;
import com.camp.service.CampPackageService;
import com.camp.service.EquipmentService;
import com.camp.service.WeatherService;
import com.camp.vo.PackageVO;
import com.camp.vo.RecommendVO;
import com.camp.vo.WeatherVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 天气 AI 套餐推荐接口
 * <p>流程：校验日期（未来 7 天内）→ 调用天气接口取当日天气 → 调用大模型（传日期/地点/天气/套餐列表）
 * → 返回风险等级、场景类型、推荐套餐、附加装备与提醒。</p>
 * <p>日期不在未来 7 天内时直接返回提示，不调用天气与 AI。</p>
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherRecommendController {

    /** 允许推荐的天数范围（未来 N 天，含今天） */
    private static final int DAYS_RANGE = 7;

    @Resource
    private WeatherService weatherService;

    @Resource
    private AiRecommendService aiRecommendService;

    @Resource
    private CampPackageService campPackageService;

    @Resource
    private EquipmentService equipmentService;

    /**
     * 生成天气 AI 套餐推荐
     *
     * @param dto 日期 + 地点
     * @return 推荐结果
     */
    @PostMapping("/recommend")
    public Result<RecommendVO> recommend(@Valid @RequestBody RecommendDTO dto) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(DAYS_RANGE);
        LocalDate date = dto.getDate();

        // 需求：日期不在未来 7 天内，直接返回提示，不调用 AI
        if (date.isBefore(today) || date.isAfter(end)) {
            return Result.error("只能选择未来 " + DAYS_RANGE + " 天内的日期（" + today + " 至 " + end + "），请重新选择");
        }

        String location = dto.getLocation().trim();

        // 1. 获取当日天气
        WeatherVO weather = weatherService.getWeather(date, location);

        // 2. 取系统在架套餐
        List<PackageVO> packages = campPackageService.listPackages(null);

        // 3. 交给人（大）模型按规则生成推荐
        RecommendVO vo = aiRecommendService.recommend(date, location, weather, packages);

        // 4. 追加适配的露营食品作为可选附加项（食品为一次性售卖，不属于套餐）
        List<Equipment> foods = equipmentService.list(new LambdaQueryWrapper<Equipment>()
                .eq(Equipment::getProductType, 2)
                .eq(Equipment::getStatus, 1)
                .orderByDesc(Equipment::getStockNum)
                .last("LIMIT 3"));
        if (!foods.isEmpty()) {
            List<Equipment> optionalAdd = vo.getOptionalAdd() == null
                    ? new ArrayList<>() : new ArrayList<>(vo.getOptionalAdd());
            for (Equipment food : foods) {
                boolean exists = optionalAdd.stream().anyMatch(e -> e.getId().equals(food.getId()));
                if (!exists) {
                    optionalAdd.add(food);
                }
            }
            vo.setOptionalAdd(optionalAdd);
        }
        return Result.success(vo);
    }
}
