package com.camp.aop;

import com.camp.annotation.OperLog;
import com.camp.entity.SysOperLog;
import com.camp.security.LoginUser;
import com.camp.service.OperLogService;
import com.camp.util.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 操作日志切面：记录操作人、时间、内容、耗时、IP
 *
 * @author camp
 */
@Slf4j
@Aspect
@Component
public class OperLogAspect {

    @Resource
    private OperLogService operLogService;
    @Resource
    private ObjectMapper objectMapper;

    @Around("@annotation(com.camp.annotation.OperLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        SysOperLog entity = new SysOperLog();
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            OperLog annotation = signature.getMethod().getAnnotation(OperLog.class);
            entity.setModule(annotation == null ? "" : annotation.module());
            entity.setAction(annotation == null ? "" : annotation.action());

            HttpServletRequest request = currentRequest();
            if (request != null) {
                entity.setMethod(request.getMethod());
                entity.setUri(request.getRequestURI());
                entity.setIp(clientIp(request));
            }
            entity.setUserId(UserContext.getUserId());
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser) {
                LoginUser user = (LoginUser) auth.getPrincipal();
                entity.setUsername(user.getUsername());
                entity.setRoleCode(user.getRole() != null && user.getRole() == 1 ? "ROLE_ADMIN" : "");
            }
            entity.setParams(maskArgs(point.getArgs()));

            Object result = point.proceed();
            entity.setResultCode(200);
            return result;
        } catch (Throwable e) {
            entity.setResultCode(500);
            entity.setErrorMsg(e.getMessage() != null && e.getMessage().length() > 480
                    ? e.getMessage().substring(0, 480) : e.getMessage());
            throw e;
        } finally {
            entity.setCostMs(System.currentTimeMillis() - start);
            try {
                operLogService.saveLog(entity);
            } catch (Exception ex) {
                log.warn("保存操作日志失败: {}", ex.getMessage());
            }
        }
    }

    /** 参数脱敏：密码、令牌不落库 */
    private String maskArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        try {
            String json = objectMapper.writeValueAsString(Arrays.asList(args));
            return json.replaceAll("(\"password\"\s*:\s*\")([^\"]*)(\")", "$1***$3")
                    .replaceAll("(\"token\"\s*:\s*\")([^\"]*)(\")", "$1***$3");
        } catch (Exception e) {
            return "";
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}
