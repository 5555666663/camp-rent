package com.camp.service;

import com.camp.vo.IpLocationVO;

/**
 * 定位服务接口
 *
 * @author camp
 */
public interface LocationService {

    /**
     * 通过请求方 IP 粗略定位到城市（高德 IP 定位，精度为城市级）
     *
     * @return 定位结果，失败返回 null
     */
    IpLocationVO ipLocation();
}
