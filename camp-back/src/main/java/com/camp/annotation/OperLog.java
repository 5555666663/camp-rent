package com.camp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解：标注在后台写操作方法上
 *
 * @author camp
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    /** 模块名，如「装备管理」 */
    String module() default "";

    /** 操作名，如「新增装备」 */
    String action() default "";
}
