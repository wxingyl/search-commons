package com.tqmall.search.common.cache;

import com.tqmall.search.common.result.ErrorCode;

/**
 * Created by xing on 15/12/26.
 * 定义Cache处理的相关异常错误
 */
public enum CacheErrorCode implements ErrorCode {

    SLAVE_REGISTER_INVALID("1001", "Slave机器注册处理错误: %s");

    private String code;

    private String message;

    CacheErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
