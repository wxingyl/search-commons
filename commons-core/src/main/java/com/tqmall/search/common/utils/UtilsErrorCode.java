package com.tqmall.search.common.utils;

import com.tqmall.search.common.result.ErrorCode;

/**
 * Created by xing on 15/12/28.
 * Utils中的ErrorCode, 只在util内部使用
 */
enum UtilsErrorCode implements ErrorCode {

    JSON_RESULT_CONVERT_INVALID_OBJECT("80210101", "Result格式Json解析, data字段不是Object: %s"),
    JSON_RESULT_CONVERT_INVALID_ARRAY("80210102", "Result格式Json解析, data字段不是数组: %s"),
    JSON_RESULT_PARSE_INVALID_STRING("80210103", "Json字符串不是Result格式, 无法解析: %s");

    private String code;

    private String message;

    UtilsErrorCode(String code, String message) {
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
