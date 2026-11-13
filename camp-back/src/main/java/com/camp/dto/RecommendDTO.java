package com.camp.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 天气 AI 推荐请求 DTO
 *
 * @author camp
 */
@Data
public class RecommendDTO implements Serializable {

    /** 出行日期（必须为未来 7 天内） */
    @NotNull(message = "请选择出行日期")
    private LocalDate date;

    /** 露营/徒步地点 */
    @NotBlank(message = "请输入露营或徒步地点")
    private String location;
}
