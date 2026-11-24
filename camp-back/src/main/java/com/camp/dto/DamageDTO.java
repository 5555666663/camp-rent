package com.camp.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 归还定损入参
 *
 * @author camp
 */
@Data
public class DamageDTO {

    /** 定损等级：0无 1轻微 2中度 3严重 */
    @NotNull(message = "定损等级不能为空")
    private Integer damageLevel;

    /** 照片地址，逗号分隔（1~9 张） */
    @NotBlank(message = "请至少上传一张定损照片")
    private String imgUrls;

    /** 赔偿金额 */
    private BigDecimal compensateAmount;

    /** 备注 */
    private String remark;
}
