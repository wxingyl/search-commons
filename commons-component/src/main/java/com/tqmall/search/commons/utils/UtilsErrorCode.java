package com.tqmall.search.commons.utils;

import com.tqmall.search.commons.result.ErrorCode;

/**
 * Created by xing on 15/12/28.
 * Utils中的ErrorCode, 只在util内部使用
 */
public enum UtilsErrorCode implements ErrorCode {

    //错误码从101开始走起 result格式json字符长解析错误码
    JSON_RESULT_CONVERT_INVALID_OBJECT(101, "Result格式Json解析, data字段不是Object: %s"),
    JSON_RESULT_CONVERT_INVALID_ARRAY(102, "Result格式Json解析, data字段不是数组: %s"),
    JSON_RESULT_PARSE_INVALID_STRING(103, "json字符串不符合Result格式: %s");

    static {
        ErrorCodeBuilder builder = new ErrorCodeBuilder(811);
        for (UtilsErrorCode e : UtilsErrorCode.values()) {
            e.code = builder.buildErrorCode(e.exceptionCode);
        }
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
