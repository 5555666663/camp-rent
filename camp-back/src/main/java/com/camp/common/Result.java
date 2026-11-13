package com.camp.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果封装
 *
 * @param <T> 数据类型
 * @author camp
 */
@Data
public class Result<T> implements Serializable {

    /** 状态码：200 成功，其它为失败 */
    private Integer code;

    /** 提示信息 */
    private String msg;

    /** 返回数据 */
    private T data;

    private Result() {
    }

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /** 成功（无数据） */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /** 成功（带数据） */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /** 成功（自定义提示） */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    /** 失败（自定义状态码和提示） */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    /** 失败（默认状态码 500） */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
}
