package com.tqmall.search.common.utils;

import com.tqmall.search.common.result.ErrorCode;

/**
 * Created by xing on 15/12/28.
 * Utils中的ErrorCode
 */
enum UtilsErrorCode implements ErrorCode {
    JSON_RESULT_CONVERT_INVALID_OBJECT("80210101", "Json中data字段不是Object: %s"),
    JSON_RESULT_CONVERT_INVALID_ARRAY("80210102", "Json中data字段不是数组: %s");
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
