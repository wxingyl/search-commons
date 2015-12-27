package com.tqmall.search.common.utils;

/**
 * Created by xing on 15/12/27.
 * Result返回结果为Json结构, 解析其结构存在异常
 */
public class ResultJsonParseException extends RuntimeException {

    private static final long serialVersionUID = 1l;

    public ResultJsonParseException(String msg) {
        super(msg);
    }
}
