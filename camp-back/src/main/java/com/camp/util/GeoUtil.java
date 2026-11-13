package com.camp.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 地理距离工具类
 * <p>
 * 采用 Haversine 公式计算两点球面距离，纯后端计算，不依赖高德接口，避免泄露 Key。
 *
 * @author camp
 */
public class GeoUtil {

    /** 地球平均半径（公里） */
    private static final double EARTH_RADIUS_KM = 6371.0088;

    private GeoUtil() {
    }

    /**
     * 计算两个经纬度之间的距离（公里，保留两位小数）
     *
     * @param lng1 起点经度
     * @param lat1 起点纬度
     * @param lng2 终点经度
     * @param lat2 终点纬度
     * @return 距离（公里），任一坐标为空时返回 null
     */
    public static Double distanceKm(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2) {
        if (lng1 == null || lat1 == null || lng2 == null || lat2 == null) {
            return null;
        }
        double radLat1 = Math.toRadians(lat1.doubleValue());
        double radLat2 = Math.toRadians(lat2.doubleValue());
        double deltaLat = radLat2 - radLat1;
        double deltaLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double km = EARTH_RADIUS_KM * c;
        return BigDecimal.valueOf(km).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
