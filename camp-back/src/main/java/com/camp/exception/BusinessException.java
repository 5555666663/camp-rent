package com.camp.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 * 用于业务逻辑中主动抛出，由全局异常处理器统一捕获返回
 *
 * @author camp
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 异常状态码 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
