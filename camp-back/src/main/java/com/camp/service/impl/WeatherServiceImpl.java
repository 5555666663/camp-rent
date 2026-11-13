package com.camp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.camp.service.WeatherService;
import com.camp.vo.WeatherVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 天气服务实现
 * <p>使用 Open-Meteo 免费天气接口（无需 Key）：</p>
 * <ul>
 *     <li>地名解析：https://geocoding-api.open-meteo.com/v1/search</li>
 *     <li>天气预报：https://api.open-meteo.com/v1/forecast</li>
 * </ul>
 * <p>若外网不可用，则退化为「按地点+日期哈希生成」的模拟天气，保证演示不中断。</p>
 *
 * @author camp
 */
@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {

    /** 天气接口地址 */
    @Value("${weather.api-base:https://api.open-meteo.com}")
    private String apiBase;

    /** 地理编码接口地址 */
    @Value("${weather.geo-base:https://geocoding-api.open-meteo.com}")
    private String geoBase;

    /** 接口超时时间（毫秒） */
    @Value("${weather.timeout:8000}")
    private int timeout;

    @Override
    public WeatherVO getWeather(LocalDate date, String location) {
        try {
            double[] pos = geocode(location);
            if (pos == null) {
                log.warn("地点解析失败，使用模拟天气：{}", location);
                return fallback(date, location);
            }
            String url = apiBase + "/v1/forecast?latitude=" + pos[0] + "&longitude=" + pos[1]
                    + "&daily=weather_code,temperature_2m_max,temperature_2m_min,"
                    + "precipitation_probability_max,wind_speed_10m_max,uv_index_max"
                    + "&timezone=Asia%2FShanghai&start_date=" + date + "&end_date=" + date;
            String resp = HttpUtil.get(url, timeout);
            if (StrUtil.isBlank(resp)) {
                return fallback(date, location);
            }
            JSONObject daily = JSONUtil.parseObj(resp).getJSONObject("daily");
            if (daily == null) {
                return fallback(date, location);
            }
            JSONArray codes = daily.getJSONArray("weather_code");
            if (codes == null || codes.isEmpty()) {
                return fallback(date, location);
            }
            int code = codes.getInt(0);

            WeatherVO vo = new WeatherVO();
            vo.setDate(date.toString());
            vo.setLocation(location);
            vo.setWeatherText(weatherText(code));
            vo.setTempMin(daily.getJSONArray("temperature_2m_min").getDouble(0));
            vo.setTempMax(daily.getJSONArray("temperature_2m_max").getDouble(0));
            vo.setPrecipitationProbability(daily.getJSONArray("precipitation_probability_max").getInt(0));
            vo.setWindSpeed(daily.getJSONArray("wind_speed_10m_max").getDouble(0));
            vo.setUvIndex(daily.getJSONArray("uv_index_max").getDouble(0));
            vo.setWillRain(isRainCode(code));
            vo.setThunderAlert(code >= 95);
            vo.setSource("open-meteo");
            return vo;
        } catch (Exception e) {
            log.warn("天气接口调用失败，使用模拟天气：{}", e.getMessage());
            return fallback(date, location);
        }
    }

    /**
     * 地点解析：地名 -> [纬度, 经度]
     */
    private double[] geocode(String location) {
        try {
            String url = geoBase + "/v1/search?name=" + java.net.URLEncoder.encode(location, "UTF-8")
                    + "&count=1&language=zh&format=json";
            String resp = HttpUtil.get(url, timeout);
            if (StrUtil.isBlank(resp)) {
                return null;
            }
            JSONArray results = JSONUtil.parseObj(resp).getJSONArray("results");
            if (results == null || results.isEmpty()) {
                return null;
            }
            JSONObject first = results.getJSONObject(0);
            return new double[]{first.getDouble("latitude"), first.getDouble("longitude")};
        } catch (Exception e) {
            log.warn("地点解析异常：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 天气代码 -> 中文描述（WMO Weather interpretation codes）
     */
    private String weatherText(int code) {
        if (code == 0) {
            return "晴";
        } else if (code == 1) {
            return "晴间多云";
        } else if (code == 2) {
            return "多云";
        } else if (code == 3) {
            return "阴";
        } else if (code == 45 || code == 48) {
            return "雾";
        } else if (code >= 51 && code <= 57) {
            return "毛毛雨";
        } else if (code >= 61 && code <= 65) {
            return "雨";
        } else if (code == 66 || code == 67) {
            return "冻雨";
        } else if (code >= 71 && code <= 77) {
            return "雪";
        } else if (code >= 80 && code <= 82) {
            return "阵雨";
        } else if (code == 85 || code == 86) {
            return "阵雪";
        } else if (code == 95) {
            return "雷阵雨";
        } else if (code >= 96) {
            return "雷阵雨伴冰雹";
        }
        return "未知";
    }

    /**
     * 是否属于降雨类天气代码
     */
    private boolean isRainCode(int code) {
        return (code >= 51 && code <= 67) || (code >= 80 && code <= 82) || code >= 95;
    }

    /**
     * 模拟天气（外网不可用时兜底）：按「地点+日期」哈希生成稳定结果
     */
    private WeatherVO fallback(LocalDate date, String location) {
        String key = location + date;
        int seed = Math.abs(key.hashCode());
        // 天气代码池：晴、多云、阴、小雨、阵雨、雷阵雨
        int[] pool = {0, 1, 2, 3, 61, 80, 95};
        int code = pool[seed % pool.length];

        WeatherVO vo = new WeatherVO();
        vo.setDate(date.toString());
        vo.setLocation(location);
        vo.setWeatherText(weatherText(code));
        vo.setTempMin(12.0 + seed % 8);
        vo.setTempMax(20.0 + seed % 12);
        vo.setPrecipitationProbability(code >= 61 ? 60 + seed % 40 : seed % 30);
        vo.setWindSpeed(8.0 + seed % 20);
        vo.setUvIndex(code == 0 ? 6.0 + seed % 4 : 2.0 + seed % 4);
        vo.setWillRain(isRainCode(code));
        vo.setThunderAlert(code >= 95);
        vo.setSource("fallback");
        return vo;
    }
}
