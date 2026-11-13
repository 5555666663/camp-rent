package com.camp.service;

import com.camp.vo.WeatherVO;

import java.time.LocalDate;

/**
 * 天气服务
 * <p>根据地点与日期获取当日天气要素（温度、降水概率、是否下雨、风力、紫外线、雷电预警）。</p>
 *
 * @author camp
 */
public interface WeatherService {

    /**
     * 获取指定地点、指定日期的天气
     *
     * @param date     出行日期
     * @param location 地点名称（城市或景区）
     * @return 天气信息
     */
    WeatherVO getWeather(LocalDate date, String location);
}
