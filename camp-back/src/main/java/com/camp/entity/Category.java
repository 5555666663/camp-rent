package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 装备分类实体
 *
 * @author camp
 */
@Data
@TableName("category")
public class Category implements Serializable {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称 */
    private String categoryName;

    /** 分类类型：1-露营装备，2-露营食品 */
    private Integer categoryType;

    /** 排序值（越小越靠前） */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createTime;
}
