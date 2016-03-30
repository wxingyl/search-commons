package com.tqmall.search.commons.exception;

/**
 * Created by xing on 16/1/4.
 * 缓存初始换异常
 */
public class MemoryCacheInitException extends RuntimeException {

    private static final long serialVersionUID = -1274658907899391240L;

    public MemoryCacheInitException(String message) {
        super(message);
    }

    public MemoryCacheInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public MemoryCacheInitException(Throwable cause) {
        super(cause);
    }
}
