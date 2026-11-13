package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.camp.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 装备分类 Mapper
 *
 * @author camp
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
