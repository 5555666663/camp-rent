package com.camp.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.entity.Role;
import com.camp.entity.User;
import com.camp.entity.UserRole;
import com.camp.mapper.RoleMapper;
import com.camp.mapper.UserRoleMapper;
import com.camp.service.UserService;
import com.camp.vo.UserVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理接口（仅超级管理员）
 * 包含：用户分页查询、新增客服账号、启用/禁用账号
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Resource
    private UserService userService;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    /**
     * 用户分页查询（可按用户名/姓名/手机号搜索）
     */
    @GetMapping("/page")
    public Result<PageResult<UserVO>> page(@RequestParam(defaultValue = "1") long current,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getId);
        IPage<User> page = userService.page(new Page<>(current, size), wrapper);
        IPage<UserVO> voPage = page.convert(user -> toVO(user));
        return Result.success(PageResult.of(voPage));
    }

    /**
     * 新增客服账号（自动绑定 ROLE_CS 角色）
     */
    @PostMapping("/cs")
    public Result<Void> createCs(@RequestBody User body) {
        if (StrUtil.isBlank(body.getUsername()) || StrUtil.isBlank(body.getPassword())) {
            return Result.error("用户名和密码不能为空");
        }
        long exists = userService.count(new LambdaQueryWrapper<User>().eq(User::getUsername, body.getUsername()));
        if (exists > 0) {
            return Result.error("用户名已存在");
        }
        User user = new User();
        user.setUsername(body.getUsername());
        user.setPassword(SecureUtil.md5(body.getPassword()));
        user.setRealName(StrUtil.blankToDefault(body.getRealName(), "客服"));
        user.setPhone(body.getPhone());
        user.setRole(0);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        userService.save(user);

        Role csRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, "ROLE_CS"));
        if (csRole != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(csRole.getId());
            userRole.setCreateTime(LocalDateTime.now());
            userRoleMapper.insert(userRole);
        }
        return Result.success();
    }

    /**
     * 启用 / 禁用账号
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 实体转 VO，并补充角色编码
     */
    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(user.getId());
        vo.setRoles(roles == null ? new ArrayList<>() : roles);
        return vo;
    }
}
