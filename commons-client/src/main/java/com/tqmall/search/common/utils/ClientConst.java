package com.tqmall.search.common.utils;

/**
 * Created by xing on 16/1/22.
 * Client 包常量定义
 */
public interface ClientConst {
    /**
     * 系统码长度
     */
    int SYSTEM_CODE_LENGTH = 3;

    /**
     * 具体异常码长度
     */
    int EXCEPTION_CODE_LENGTH = 4;
    /**
     * 异常码的长度
     */
    int ERROR_CODE_LENGTH = SYSTEM_CODE_LENGTH + 1 + EXCEPTION_CODE_LENGTH;

    /**
     * 错误码格式
     */
    String ERROR_CODE_FORMAT = "%0" + SYSTEM_CODE_LENGTH + "d%1d%0" + EXCEPTION_CODE_LENGTH + "d";

    /**
     * 默认的分割符separator
     */
    char SEPARATOR_CHAR = ',';

    String SEPARATOR = "" + SEPARATOR_CHAR;
    /**
     * 默认的区间范围字符
     */
    char RANGE_FILTER_CHAR = '~';

    String RANGE_FILTER = "" + RANGE_FILTER_CHAR;
}
