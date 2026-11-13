package com.camp.security;

import cn.hutool.core.convert.Convert;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类（基于 Hutool）
 * 负责 token 的生成与解析校验
 *
 * @author camp
 */
@Component
public class JwtUtil {

    /** 签名密钥 */
    @Value("${jwt.secret}")
    private String secret;

    /** 过期时间（毫秒） */
    @Value("${jwt.expire}")
    private long expire;

    /**
     * 生成 token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色
     */
    public String createToken(Long userId, String username, Integer role) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("uid", userId);
        payload.put("username", username);
        payload.put("role", role);
        // 设置过期时间
        payload.put(JWT.EXPIRES_AT, System.currentTimeMillis() + expire);
        return JWTUtil.createToken(payload, secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 校验 token 是否有效（校验签名与过期时间）
     */
    public boolean verify(String token) {
        try {
            return JWTUtil.verify(token, secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 token 中解析出 JWT 对象
     */
    private JWT parse(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        jwt.setKey(secret.getBytes(StandardCharsets.UTF_8));
        return jwt;
    }

    /** 获取用户ID */
    public Long getUserId(String token) {
        return Convert.toLong(parse(token).getPayload("uid"));
    }

    /** 获取用户名 */
    public String getUsername(String token) {
        return Convert.toStr(parse(token).getPayload("username"));
    }

    /** 获取角色 */
    public Integer getRole(String token) {
        return Convert.toInt(parse(token).getPayload("role"));
    }
}
