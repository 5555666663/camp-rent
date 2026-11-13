package com.camp.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.camp.entity.Store;
import com.camp.mapper.StoreMapper;
import com.camp.service.LocationService;
import com.camp.vo.IpLocationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 定位服务实现（IP 定位兜底，城市级精度）
 * ------------------------------------------------------------
 * 1. 优先使用高德 IP 定位接口（需要「Web服务」类型的 Key，返回城市中心经纬度）
 * 2. 高德不可用（Key 平台不匹配 / 未配置）时，改用 pconline 免费接口只取城市名，
 *    再用该城市所有网点的坐标均值作为城市中心，保证「同城网点优先」的准确性
 * 3. 结果缓存 10 分钟，避免频繁请求第三方接口
 *
 * @author camp
 */
@Slf4j
@Service
public class LocationServiceImpl implements LocationService {

    /** 高德 Web 服务 Key（需为「Web服务」平台类型） */
    @Value("${amap.web-key:}")
    private String webKey;

    @Resource
    private StoreMapper storeMapper;

    private static final String AMAP_IP_URL = "https://restapi.amap.com/v3/ip";
    private static final String PCONLINE_IP_URL = "https://whois.pconline.com.cn/ipJson.jsp?json=true";
    private static final long CACHE_MS = 10 * 60 * 1000L;

    private volatile IpLocationVO cache;
    private volatile long cacheTime = 0L;

    @Override
    public IpLocationVO ipLocation() {
        long now = System.currentTimeMillis();
        if (cache != null && now - cacheTime < CACHE_MS) {
            return cache;
        }
        IpLocationVO vo = fromAmap();
        if (vo == null || vo.getLongitude() == null) {
            vo = fromPconline();
        }
        if (vo != null) {
            cache = vo;
            cacheTime = now;
        }
        return vo;
    }

    /** 方式一：高德 IP 定位（最准的城市中心） */
    private IpLocationVO fromAmap() {
        if (webKey == null || webKey.trim().isEmpty()) {
            return null;
        }
        try {
            String body = HttpUtil.get(AMAP_IP_URL + "?key=" + webKey, 5000);
            JSONObject json = JSONUtil.parseObj(body);
            if (!"1".equals(json.getStr("status"))) {
                log.warn("高德 IP 定位不可用（{}），改用备用方案", json.getStr("info"));
                return null;
            }
            String rect = json.getStr("rectangle");
            if (rect == null || !rect.contains(";")) {
                return null;
            }
            String[] two = rect.split(";");
            String[] p1 = two[0].split(",");
            String[] p2 = two[1].split(",");
            double lng = (Double.parseDouble(p1[0]) + Double.parseDouble(p2[0])) / 2;
            double lat = (Double.parseDouble(p1[1]) + Double.parseDouble(p2[1])) / 2;

            IpLocationVO vo = new IpLocationVO();
            String province = json.getStr("province");
            String city = json.getStr("city");
            vo.setProvince(province);
            vo.setCity(city == null || city.isEmpty() || "[]".equals(city) ? province : city);
            vo.setLongitude(BigDecimal.valueOf(lng).setScale(6, RoundingMode.HALF_UP));
            vo.setLatitude(BigDecimal.valueOf(lat).setScale(6, RoundingMode.HALF_UP));
            return vo;
        } catch (Exception e) {
            log.warn("高德 IP 定位异常：{}", e.getMessage());
            return null;
        }
    }

    /** 方式二：pconline 只取城市名，再用同城网点坐标均值作为城市中心 */
    private IpLocationVO fromPconline() {
        try {
            String body = HttpUtil.get(PCONLINE_IP_URL, 5000);
            JSONObject json = JSONUtil.parseObj(body);
            String city = json.getStr("city");
            String pro = json.getStr("pro");
            if (city == null || city.trim().isEmpty()) {
                city = pro;
            }
            if (city == null || city.trim().isEmpty()) {
                return null;
            }
            // 去掉「市/省/自治区」等后缀，用于模糊匹配网点所在城市
            String shortCity = city.replaceAll("(市|省|自治区|特别行政区)$", "");
            if (shortCity.length() < 2) {
                return null;
            }
            List<Store> stores = storeMapper.selectList(
                    new LambdaQueryWrapper<Store>()
                            .eq(Store::getStatus, 1)
                            .like(Store::getCity, shortCity));
            IpLocationVO vo = new IpLocationVO();
            vo.setProvince(pro);
            vo.setCity(city);
            if (stores.isEmpty()) {
                // 城市没有网点时，用首个城市名直接返回，交给前端提示
                return null;
            }
            double sumLng = 0;
            double sumLat = 0;
            int valid = 0;
            for (Store s : stores) {
                if (s.getLongitude() != null && s.getLatitude() != null) {
                    sumLng += s.getLongitude().doubleValue();
                    sumLat += s.getLatitude().doubleValue();
                    valid++;
                }
            }
            if (valid == 0) {
                return null;
            }
            vo.setLongitude(BigDecimal.valueOf(sumLng / valid).setScale(6, RoundingMode.HALF_UP));
            vo.setLatitude(BigDecimal.valueOf(sumLat / valid).setScale(6, RoundingMode.HALF_UP));
            return vo;
        } catch (Exception e) {
            log.warn("pconline IP 定位异常：{}", e.getMessage());
            return null;
        }
    }
}
