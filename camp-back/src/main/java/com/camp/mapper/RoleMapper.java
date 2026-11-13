package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.camp.entity.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色 Mapper
 *
 * @author camp
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
