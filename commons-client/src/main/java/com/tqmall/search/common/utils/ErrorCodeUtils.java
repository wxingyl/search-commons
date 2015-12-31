package com.tqmall.search.common.utils;

import com.tqmall.search.common.exception.SystemCodeOverflowException;
import com.tqmall.search.common.result.ErrorCode;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xing on 15/12/31.
 * {@link com.tqmall.search.common.result.ErrorCode} 的一些工具方法
 * 注意, 构造ErrorCode的具体code的相关方法调用是在定义错误码的时候调用
 */
public class ErrorCodeUtils {
    /**
     * 系统码长度
     */
    private static final int SYSTEM_CODE_LENGTH = 3;

    /**
     * 具体异常码长度
     */
    private static final int EXCEPTION_CODE_LENGTH = 4;

    private static final String ERROR_CODE_FORMAT = "%0" + SYSTEM_CODE_LENGTH + "d%1d%0" + EXCEPTION_CODE_LENGTH + "d";

    /**
     * 设定的系统码, 可以实时添加已经溢出, 最多100个类调用添加
     */
    private static final ConcurrentMap<String, Integer> SYSTEM_CODE_MAP = new ConcurrentHashMap<>();

    /**
     * 默认的SystemCode, 对, 你没有看错, 800就是搜索
     */
    private static final int DEFAULT_SYSTEM_CODE = 800;

    private static int getSystemCode(Class cls) {
        Integer code = SYSTEM_CODE_MAP.get(cls.getName());
        return code == null ? DEFAULT_SYSTEM_CODE : code;
    }

    /**
     * 包装对于Message中存在变量的{@link ErrorCode}
     * @param errorCode 原始定义的{@link ErrorCode}
     * @param args 参数, 如果大小为0, 则不进行包装
     * @return 参数, 如果大小为0, 则不进行包装
     */
    public static ErrorCode wrapperErrorCode(final ErrorCode errorCode, Object... args) {
        if (args.length == 0) return errorCode;
        final String message = String.format(errorCode.getMessage(), args);
        return new ErrorCode() {
            @Override
            public String getCode() {
                return errorCode.getCode();
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    public static void setSystemCode(Class cls, int code) {
        if (SYSTEM_CODE_MAP.size() > 100) {
            throw new SystemCodeOverflowException(100);
        }
        SYSTEM_CODE_MAP.put(cls.getName(), code);
    }

    @CallerSensitive
    public static void setSystemCode(int code) {
        setSystemCode(Reflection.getCallerClass(), code);
    }

    /**
     * 移除调用该方法的类注册的{@link #setSystemCode(int)}系统码
     * @see #setSystemCode(int)
     */
    @CallerSensitive
    public static void removeSystemCode() {
        SYSTEM_CODE_MAP.remove(Reflection.getCallerClass().getName());
    }

    /**
     * 使用的系统码为调用该方法的类注册的{@link #setSystemCode(int)}系统码, 没有注册则默认为0
     * @see #setSystemCode(int)
     */
    @CallerSensitive
    public static String buildFatalCode(int exceptionCode) {
        return buildCode(getSystemCode(Reflection.getCallerClass()), ErrorCode.Level.FATAL, exceptionCode);
    }

    /**
     * 使用的系统码为调用该方法的类注册的{@link #setSystemCode(int)}系统码, 没有注册则默认为0
     * @see #setSystemCode(int)
     */
    @CallerSensitive
    public static String buildErrorCode(int exceptionCode) {
        return buildCode(getSystemCode(Reflection.getCallerClass()), ErrorCode.Level.ERROR, exceptionCode);
    }

    /**
     * 使用的系统码为调用该方法的类注册的{@link #setSystemCode(int)}系统码, 没有注册则默认为0
     * @see #setSystemCode(int)
     */
    @CallerSensitive
    public static String buildWarnCode(int exceptionCode) {
        return buildCode(getSystemCode(Reflection.getCallerClass()), ErrorCode.Level.WARN, exceptionCode);
    }

    /**
     * 使用的系统码为调用该方法的类注册的{@link #setSystemCode(int)}系统码, 没有注册则默认为0
     * @see #setSystemCode(int)
     */
    @CallerSensitive
    public static String buildCode(ErrorCode.Level level, int exceptionCode) {
        return buildCode(getSystemCode(Reflection.getCallerClass()), level, exceptionCode);
    }

    public static String buildFatalCode(int systemCode, int exceptionCode) {
        return buildCode(systemCode, ErrorCode.Level.FATAL, exceptionCode);
    }

    public static String buildErrorCode(int systemCode, int exceptionCode) {
        return buildCode(systemCode, ErrorCode.Level.ERROR, exceptionCode);
    }

    public static String buildWarnCode(int systemCode, int exceptionCode) {
        return buildCode(systemCode, ErrorCode.Level.WARN, exceptionCode);
    }

    public static String buildCode(int systemCode, ErrorCode.Level level, int exceptionCode) {
        return String.format(ERROR_CODE_FORMAT, systemCode, level.getCode(), exceptionCode);
    }

}
