package com.camp.util;

/**
 * 当前登录用户上下文（ThreadLocal）
 * 由 JwtAuthenticationFilter 在请求进入时写入，请求结束时清理
 *
 * @author camp
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> ROLE = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /** 获取当前登录用户ID（未登录返回 null） */
    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setRole(Integer role) {
        ROLE.set(role);
    }

    /** 获取当前登录用户角色 */
    public static Integer getRole() {
        return ROLE.get();
    }

    /** 清理线程变量 */
    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}
