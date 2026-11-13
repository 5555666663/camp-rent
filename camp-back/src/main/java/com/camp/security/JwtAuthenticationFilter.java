package com.camp.security;

import com.camp.mapper.UserRoleMapper;
import com.camp.util.UserContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JWT 认证过滤器
 * 从请求头 Authorization 中解析 token，校验通过后设置 SecurityContext
 * <p>RBAC：从 user_role 关联表加载用户的全部角色（ROLE_USER / ROLE_CS / ROLE_ADMIN），
 * 一个用户可以同时拥有多个角色。</p>
 *
 * @author camp
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Resource
    private UserRoleMapper userRoleMapper;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (StringUtils.hasText(token) && jwtUtil.verify(token)) {
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            Integer role = jwtUtil.getRole(token);

            LoginUser loginUser = new LoginUser(userId, username, role);

            // 从 user_role 加载角色编码，映射为 Spring Security 权限
            List<String> roleCodes = new ArrayList<>();
            try {
                roleCodes = userRoleMapper.selectRoleCodesByUserId(userId);
            } catch (Exception ignored) {
                // 数据库异常时降级为旧版单角色逻辑
            }
            if (roleCodes == null || roleCodes.isEmpty()) {
                roleCodes = Collections.singletonList((role != null && role == 1) ? "ROLE_ADMIN" : "ROLE_USER");
            }
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for (String code : roleCodes) {
                authorities.add(new SimpleGrantedAuthority(code));
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    loginUser, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 存入 ThreadLocal，方便业务层获取当前登录用户
            UserContext.setUserId(userId);
            UserContext.setRole(role);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清理 ThreadLocal，防止线程复用导致数据串号
            UserContext.clear();
        }
    }

    /**
     * 从请求头解析 token（格式：Authorization: Bearer xxx）
     */
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
