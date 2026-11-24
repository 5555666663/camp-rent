package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐规则（可视化配置）
 *
 * @author camp
 */
@Data
@TableName("recommend_rule")
public class RecommendRule {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则名称 */
    private String ruleName;

    /** 优先级（越小越先命中） */
    private Integer priority;

    /** 条件 JSON */
    private String conditionJson;

    /** 动作 JSON */
    private String actionJson;

    /** 1启用 0停用 */
    private Integer enabled;

    /** 版本号 */
    private Integer version;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
