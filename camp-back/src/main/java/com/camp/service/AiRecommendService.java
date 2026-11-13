package com.camp.service;

import com.camp.vo.PackageVO;
import com.camp.vo.RecommendVO;
import com.camp.vo.WeatherVO;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 套餐推荐服务
 * <p>把日期、地点、天气、系统套餐列表交给大模型，由大模型按规则返回推荐 JSON；
 * 未配置大模型或调用失败时，自动回退到本地规则引擎。</p>
 *
 * @author camp
 */
public interface AiRecommendService {

    /**
     * 生成推荐结果
     *
     * @param date     出行日期
     * @param location 地点
     * @param weather  当日天气
     * @param packages 系统在架套餐列表
     * @return 推荐结果（risk_level、risk_tip、scene_type、recommend_package、optional_add、remind_text）
     */
    RecommendVO recommend(LocalDate date, String location, WeatherVO weather, List<PackageVO> packages);
}
