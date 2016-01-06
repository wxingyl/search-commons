package com.tqmall.search.common.utils;

import com.tqmall.search.common.result.ErrorCode;

/**
 * Created by xing on 15/12/28.
 * Utils中的ErrorCode, 只在util内部使用
 */
public enum UtilsErrorCode implements ErrorCode {

    //缓存处理的错误码
    CACHE_SLAVE_REGISTER_ARG_INVALID(1, "Slave机器注册参数不全"),
    CACHE_SLAVE_REGISTER_INVALID(2, "Slave机器注册处理错误: %s"),
    CACHE_SLAVE_UNREGISTER_ARG_INVALID(11, "Slave机器注销参数不全"),
    CACHE_SLAVE_UNREGISTER_UNKNOWN_HOST(12, "Slave机器注销时未知的Host地址: %s"),
    CACHE_SLAVE_MONITOR_ARG_INVALID(21, "Slave机器监控请求参数不全"),

    //错误码从101开始走起 result格式json字符长解析错误码
    JSON_RESULT_CONVERT_INVALID_OBJECT(101, "Result格式Json解析, data字段不是Object: %s"),
    JSON_RESULT_CONVERT_INVALID_ARRAY(102, "Result格式Json解析, data字段不是数组: %s"),
    JSON_RESULT_PARSE_INVALID_STRING(103, "json字符串不符合Result格式: %s");

    static {
        ErrorCodeUtils.setSystemCode(802);
        for (UtilsErrorCode e : UtilsErrorCode.values()) {
            e.code = ErrorCodeUtils.buildErrorCode(e.exceptionCode);
        }
        ErrorCodeUtils.removeSystemCode();
    }

    private int exceptionCode;

    private String code;

    private String message;

    UtilsErrorCode(int exceptionCode, String message) {
        this.exceptionCode = exceptionCode;
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
