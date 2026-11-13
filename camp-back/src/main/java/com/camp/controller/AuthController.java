package com.camp.controller;

import cn.hutool.core.bean.BeanUtil;
import com.camp.common.Result;
import com.camp.dto.LoginDTO;
import com.camp.dto.RegisterDTO;
import com.camp.entity.User;
import com.camp.service.UserService;
import com.camp.util.UserContext;
import com.camp.vo.LoginVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 认证接口（注册 / 登录 / 退出 / 当前用户信息）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功", null);
    }

    /**
     * 用户登录（普通用户与管理员共用，通过 role 区分）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success("登录成功", userService.login(dto));
    }

    /**
     * 退出登录（JWT 无状态，前端清除 token 即可）
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<LoginVO> info() {
        Long userId = UserContext.getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(401, "未登录");
        }
        LoginVO vo = new LoginVO();
        BeanUtil.copyProperties(user, vo);
        return Result.success(vo);
    }
}
