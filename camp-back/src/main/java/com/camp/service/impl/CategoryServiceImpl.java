package com.camp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.entity.Category;
import com.camp.mapper.CategoryMapper;
import com.camp.service.CategoryService;
import org.springframework.stereotype.Service;

/**
 * 装备分类服务实现
 *
 * @author camp
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
