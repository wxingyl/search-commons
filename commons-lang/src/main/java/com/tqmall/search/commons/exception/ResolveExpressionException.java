package com.tqmall.search.commons.exception;

/**
 * Created by xing on 16/3/31.
 * 条件表达式违法, 解析异常
 *
 * @author xing
 */
public class ResolveExpressionException extends Exception {

    static final long serialVersionUID = 1L;

    public ResolveExpressionException(String message) {
        super(message);
    }
}
