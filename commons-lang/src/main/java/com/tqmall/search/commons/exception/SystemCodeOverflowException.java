package com.tqmall.search.commons.exception;

/**
 * Created by xing on 15/12/31.
 * SystemCode太多,可能存在溢出
 */
public class SystemCodeOverflowException extends RuntimeException {

    private static final long serialVersionUID = 401611950026767621L;

    public SystemCodeOverflowException(String message) {
        super(message);
    }

    public SystemCodeOverflowException(int size) {
        this("添加的SystemCode太多, 已经超过: " + size + "个, 应该先移除一些");
    }
}
