package com.camp.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * IP 定位结果
 *
 * @author camp
 */
@Data
public class IpLocationVO {

    /** 省份 */
    private String province;

    /** 城市（部分 IP 只能定位到省份，此时为省份名） */
    private String city;

    /** 经度（取定位矩形中心） */
    private BigDecimal longitude;

    /** 纬度（取定位矩形中心） */
    private BigDecimal latitude;

    /** 定位来源：ip */
    private String source = "ip";
}
