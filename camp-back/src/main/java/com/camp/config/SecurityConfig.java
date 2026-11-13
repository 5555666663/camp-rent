package com.camp.config;

import com.camp.common.Result;
import com.camp.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * Spring Security 配置类
 * 实现简单的登录权限控制：
 *   - /api/auth/login、/api/auth/register 放行
 *   - 前台公开浏览接口（网点、分类、装备的 GET 请求）放行
 *   - /api/admin/** 需要管理员权限
 *   - 其余接口需要登录
 *
 * @author camp
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 前后端分离 + JWT，无需 CSRF
                .csrf().disable()
                // 开启跨域（使用 CorsConfig 中的配置）
                .cors().and()
                // 无状态会话
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 权限规则
                .authorizeRequests()
                // 登录、注册放行
                .antMatchers("/api/auth/login", "/api/auth/register").permitAll()
                // 前台公开浏览接口放行（GET）
                .antMatchers(HttpMethod.GET, "/api/store/**", "/api/category/**", "/api/equipment/**").permitAll()
                // 套餐浏览放行（GET），天气 AI 推荐放行（POST，无需登录即可试用）
                .antMatchers(HttpMethod.GET, "/api/package/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/location/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/weather/recommend").permitAll()
                // 管理端接口需要管理员权限
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                // RBAC：客服工作台接口，仅客服与超级管理员可访问
                .antMatchers("/api/cs/**").hasAnyRole("CS", "ADMIN")
                // 其余接口需要登录
                .anyRequest().authenticated()
                .and()
                // 未登录 / 无权限时的统一 JSON 返回
                .exceptionHandling()
                .authenticationEntryPoint((request, response, e) -> writeJson(response, 401, "未登录或登录已过期"))
                .accessDeniedHandler((request, response, e) -> writeJson(response, 403, "无权限访问"))
                .and()
                // 在用户名密码认证过滤器之前加入 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 输出统一 JSON 错误信息
     */
    private void writeJson(javax.servlet.http.HttpServletResponse response, int code, String msg) throws java.io.IOException {
        response.setStatus(200);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(new ObjectMapper().writeValueAsString(Result.error(code, msg)));
    }
}
