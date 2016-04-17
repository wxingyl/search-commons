package com.tqmall.search.commons.result;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by xing on 15/12/4.
 * 公共的result 模板
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 7771204280803886506L;

    private boolean success;

    private String code;

    private String message;

    private T data;

    protected Result() {
    }

    /**
     * 成功的返回结构
     *
     * @param data 数据
     */
    public Result(T data) {
        success = true;
        code = "0";
        message = "";
        this.data = data;
    }

    /**
     * 失败的返回结果
     */
    public Result(ErrorCode errorCode) {
        success = false;
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public String getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    /**
     * 与{@link ErrorCode}对象比较是否相等, 同方法{@link ResultUtils#equals(Result, ErrorCode)}
     *
     * @param errorCode 具体的errorCode对象
     * @see ResultUtils#equals(Result, ErrorCode)
     */
    public final boolean isEquals(ErrorCode errorCode) {
        return Objects.equals(code, errorCode.getCode());
    }

    protected void setCode(String code) {
        this.code = code;
    }

    protected void setData(T data) {
        this.data = data;
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    protected void setSuccess(boolean success) {
        this.success = success;
    }
}
