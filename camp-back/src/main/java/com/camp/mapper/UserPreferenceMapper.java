package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.camp.entity.UserPreference;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户偏好 Mapper
 *
 * @author camp
 */
@Mapper
public interface UserPreferenceMapper extends BaseMapper<UserPreference> {
}
