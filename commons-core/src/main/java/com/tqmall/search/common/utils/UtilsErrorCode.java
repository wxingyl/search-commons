package com.tqmall.search.common.utils;

import com.tqmall.search.common.result.ErrorCode;

/**
 * Created by xing on 15/12/28.
 * Utils中的ErrorCode, 只在util内部使用
 */
public enum UtilsErrorCode implements ErrorCode {

    //notify相关错误码
    NOTIFY_HANDLE_ARG_INVALID(1, "参数不全, notifier无法处理外部请求"),
    NOTIFY_RUNTIME_ERROR(2, "notifier执行出错, %s"),
    NOTIFY_UNREGISTER_UNKNOWN_HOST(3, "未知的Host地址: %s, notifier无法处理注销请求"),

    RECEIVER_RUNTIME_ERROR(21, "receiver执行出错, %s"),

    HOST_INFO_INVALID(41, "HostInfo对象值错误: %s"),

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
