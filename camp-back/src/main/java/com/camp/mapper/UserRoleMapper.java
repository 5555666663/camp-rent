package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.camp.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联 Mapper
 *
 * @author camp
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 查询用户拥有的角色编码列表（如 ROLE_ADMIN、ROLE_CS）
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    @Select("SELECT r.role_code FROM user_role ur INNER JOIN role r ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询用户拥有的角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("SELECT r.* FROM user_role ur INNER JOIN role r ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<com.camp.entity.Role> selectRolesByUserId(@Param("userId") Long userId);
}
