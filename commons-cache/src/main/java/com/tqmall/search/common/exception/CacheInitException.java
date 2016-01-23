package com.tqmall.search.common.exception;

/**
 * Created by xing on 16/1/4.
 * 缓存初始换异常
 */
public class CacheInitException extends RuntimeException {

    private static final long serialVersionUID = -1274658907899391240L;

    public CacheInitException(String message) {
        super(message);
    }

    public CacheInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheInitException(Throwable cause) {
        super(cause);
    }
}
