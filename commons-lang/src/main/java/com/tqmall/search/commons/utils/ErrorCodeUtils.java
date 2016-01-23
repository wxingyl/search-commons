package com.tqmall.search.commons.utils;

import com.tqmall.search.commons.lang.ClientConst;
import com.tqmall.search.commons.exception.SystemCodeOverflowException;
import com.tqmall.search.commons.lang.ErrorCodeEntry;
import com.tqmall.search.commons.result.ErrorCode;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xing on 15/12/31.
 * {@link com.tqmall.search.commons.result.ErrorCode} 的一些工具方法
 * 注意, 构造ErrorCode的具体code的相关方法调用是在定义错误码的时候调用
 * 该方法用到了{@link Reflection}, 其是sun的私有API, 有一定分险, jdk1.8还在, 应该不会删除, 并且{@link Class}中大量使用,不会随意删除的吧
 */
public class ErrorCodeUtils {
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
     *
     * @param errorCode 原始定义的{@link ErrorCode}
     * @param args      参数, 如果大小为0, 则不进行包装
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
     *
     * @see #setSystemCode(int)
     */
    @CallerSensitive
    public static void removeSystemCode() {
        SYSTEM_CODE_MAP.remove(Reflection.getCallerClass().getName());
    }

    /**
     * 使用的系统码为调用该方法的类注册的{@link #setSystemCode(int)}系统码, 没有注册则默认为0
     *
     * @see #setSystemCode(int)
     */
    @CallerSensitive
    public static String buildFatalCode(int exceptionCode) {
        return buildCode(getSystemCode(Reflection.getCallerClass()), ErrorCode.Level.FATAL, exceptionCode);
    }

    /**
     * 使用的系统码为调用该方法的类注册的{@link #setSystemCode(int)}系统码, 没有注册则默认为0
     *
     * @see #setSystemCode(int)
     */
    @CallerSensitive
    public static String buildErrorCode(int exceptionCode) {
        return buildCode(getSystemCode(Reflection.getCallerClass()), ErrorCode.Level.ERROR, exceptionCode);
    }

    /**
     * 使用的系统码为调用该方法的类注册的{@link #setSystemCode(int)}系统码, 没有注册则默认为0
     *
     * @see #setSystemCode(int)
     */
    @CallerSensitive
    public static String buildWarnCode(int exceptionCode) {
        return buildCode(getSystemCode(Reflection.getCallerClass()), ErrorCode.Level.WARN, exceptionCode);
    }

    /**
     * 使用的系统码为调用该方法的类注册的{@link #setSystemCode(int)}系统码, 没有注册则默认为0
     *
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
        return String.format(ClientConst.ERROR_CODE_FORMAT, systemCode, level.getCode(), exceptionCode);
    }

    /**
     * 错误码识别, 得出具体的系统码, 错误级别以及具体的异常码
     * 错误码长度必须等于{@link ClientConst#ERROR_CODE_LENGTH}
     *
     * @see ErrorCodeEntry
     */
    public static ErrorCodeEntry parseCode(String code) {
        if (code.length() != ClientConst.ERROR_CODE_LENGTH) {
            throw new IllegalArgumentException("code: " + code + "长度不等于" + ClientConst.ERROR_CODE_LENGTH);
        }
        int levelValue = code.charAt(ClientConst.SYSTEM_CODE_LENGTH) - '0';
        ErrorCode.Level level = null;
        for (ErrorCode.Level v : ErrorCode.Level.values()) {
            if (v.getCode() == levelValue) {
                level = v;
                break;
            }
        }
        if (level == null) {
            throw new IllegalArgumentException("code: " + code + "的异常等级" + levelValue + "值错误, 找不到对应的等级");
        }
        return ErrorCodeEntry.build()
                .systemCode(StrValueConverts.intConvert(code.substring(0, ClientConst.SYSTEM_CODE_LENGTH)))
                .level(level)
                .exceptionCode(StrValueConverts.intConvert(code.substring(ClientConst.SYSTEM_CODE_LENGTH + 1)))
                .create();
    }

}
