package com.camp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.dto.LoginDTO;
import com.camp.dto.RegisterDTO;
import com.camp.entity.User;
import com.camp.exception.BusinessException;
import com.camp.mapper.UserMapper;
import com.camp.mapper.UserRoleMapper;
import com.camp.security.JwtUtil;
import com.camp.service.UserService;
import com.camp.vo.LoginVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 用户服务实现
 *
 * @author camp
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private UserRoleMapper userRoleMapper;

    /**
     * 用户注册（密码 MD5 加密存储）
     */
    @Override
    public void register(RegisterDTO dto) {
        // 用户名唯一校验
        long nameCount = count(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (nameCount > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 手机号唯一校验
        if (StrUtil.isNotBlank(dto.getPhone())) {
            long phoneCount = count(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
            if (phoneCount > 0) {
                throw new BusinessException("手机号已被注册");
            }
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(SecureUtil.md5(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setRole(0);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        save(user);
    }

    /**
     * 用户登录（返回用户信息 + token）
     */
    @Override
    public LoginVO login(LoginDTO dto) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!SecureUtil.md5(dto.getPassword()).equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        LoginVO vo = new LoginVO();
        BeanUtil.copyProperties(user, vo);
        // RBAC：加载用户绑定的全部角色编码，前端据此决定跳转页面与菜单权限
        java.util.List<String> roles = userRoleMapper.selectRoleCodesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            roles = java.util.Collections.singletonList(user.getRole() != null && user.getRole() == 1 ? "ROLE_ADMIN" : "ROLE_USER");
        }
        vo.setRoles(roles);
        vo.setToken(jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole()));
        return vo;
    }
}
