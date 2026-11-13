package com.camp.vo;

import com.camp.entity.Equipment;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 天气 AI 推荐结果 VO
 * <p>字段与需求约定的大模型 JSON 一致：risk_level、risk_tip、scene_type、
 * recommend_package、optional_add、remind_text。</p>
 *
 * @author camp
 */
@Data
public class RecommendVO implements Serializable {

    /** 风险等级：安全 / 注意 / 高危 */
    private String riskLevel;

    /** 风险提示语 */
    private String riskTip;

    /** 场景类型：露营 / 徒步 */
    private String sceneType;

    /** 推荐套餐列表 */
    private List<PackageVO> recommendPackage;

    /** 可选附加装备清单 */
    private List<Equipment> optionalAdd;

    /** 出行提醒文案 */
    private String remindText;

    /** 当日天气 */
    private WeatherVO weather;

    /** 查询日期 */
    private String date;

    /** 查询地点 */
    private String location;

    /** 推荐来源：ai / rule（大模型未配置或调用失败时走规则引擎） */
    private String source;
}
