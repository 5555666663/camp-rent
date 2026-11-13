package com.camp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.camp.dto.LoginDTO;
import com.camp.dto.RegisterDTO;
import com.camp.entity.User;
import com.camp.vo.LoginVO;

/**
 * 用户服务接口
 *
 * @author camp
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    void register(RegisterDTO dto);

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO dto);
}
