package com.camp.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 天气信息 VO
 * <p>封装指定日期、指定地点的天气要素，供 AI 推荐使用。</p>
 *
 * @author camp
 */
@Data
public class WeatherVO implements Serializable {

    /** 查询日期 yyyy-MM-dd */
    private String date;

    /** 查询地点 */
    private String location;

    /** 天气文字，如：晴 / 多云 / 小雨 / 雷阵雨 */
    private String weatherText;

    /** 最低温度（℃） */
    private Double tempMin;

    /** 最高温度（℃） */
    private Double tempMax;

    /** 降水概率（0-100） */
    private Integer precipitationProbability;

    /** 是否下雨 */
    private Boolean willRain;

    /** 最大风力（km/h） */
    private Double windSpeed;

    /** 紫外线指数（0-11+） */
    private Double uvIndex;

    /** 是否有雷电预警 */
    private Boolean thunderAlert;

    /** 天气数据来源：open-meteo / fallback */
    private String source;
}
