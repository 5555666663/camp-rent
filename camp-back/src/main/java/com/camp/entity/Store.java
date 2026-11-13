package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 网点实体
 *
 * @author camp
 */
@Data
@TableName("store")
public class Store implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 网点名称 */
    private String storeName;

    /** 所在城市 */
    private String city;

    /** 详细地址 */
    private String address;

    /** 经度 */
    private BigDecimal longitude;

    /** 纬度 */
    private BigDecimal latitude;

    /** 联系电话 */
    private String phone;

    /** 营业时间 */
    private String businessHours;

    /** 备注（取货指引等） */
    private String remark;

    /** 状态：0-停用，1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
