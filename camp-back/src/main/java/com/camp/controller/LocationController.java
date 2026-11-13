package com.camp.controller;

import com.camp.common.Result;
import com.camp.service.LocationService;
import com.camp.vo.IpLocationVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 定位接口（前台公开）
 * 浏览器不允许定位或定位不准时，可用 IP 定位兜底（城市级精度）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Resource
    private LocationService locationService;

    /**
     * IP 定位：返回城市与城市中心经纬度
     */
    @GetMapping("/ip")
    public Result<IpLocationVO> ip() {
        IpLocationVO vo = locationService.ipLocation();
        if (vo == null) {
            return Result.error("IP 定位失败，请手动选择城市或在地图页浏览网点");
        }
        return Result.success(vo);
    }
}
