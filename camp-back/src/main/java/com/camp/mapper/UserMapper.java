package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.camp.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 *
 * @author camp
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
