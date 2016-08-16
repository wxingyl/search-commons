package com.tqmall.search.commons.utils;

import com.tqmall.search.commons.lang.ErrorCodeEntry;
import com.tqmall.search.commons.result.ErrorCode;

/**
 * Created by xing on 16/2/18.
 * 实例化错误码构造器
 *
 * @see ErrorCode
 */
public class ErrorCodeBuilder {

    private final int systemCode;

    private final Config config;

    public ErrorCodeBuilder(int systemCode) {
        this(systemCode, Config.DEFAULT);
    }

    public ErrorCodeBuilder(int systemCode, Config config) {
        this.systemCode = systemCode;
        this.config = config;
    }

    public String buildCode(ErrorCode.Level level, int exceptionCode) {
        return String.format(config.getErrorCodeFormat(), systemCode, level.getCode(), exceptionCode);
    }

    public String buildFatalCode(int exceptionCode) {
        return buildCode(ErrorCode.Level.FATAL, exceptionCode);
    }

    public String buildErrorCode(int exceptionCode) {
        return buildCode(ErrorCode.Level.ERROR, exceptionCode);
    }

    public String buildWarnCode(int exceptionCode) {
        return buildCode(ErrorCode.Level.WARN, exceptionCode);
    }

    /**
     * 错误吗各个字段长度配置, 提供默认的配置{@link Config#DEFAULT}
     */
    public static class Config {
        /**
         * 系统码长度
         */
        private final int systemCodeLength;

        /**
         * 具体异常码长度
         */
        private final int exceptionCodeLength;

        /**
         * 错误码格式
         */
        private final String errorCodeFormat;

        public final static Config DEFAULT = new Config(3, 4);

        public Config(int systemCodeLength, int exceptionCodeLength) {
            this.systemCodeLength = systemCodeLength;
            this.exceptionCodeLength = exceptionCodeLength;
            errorCodeFormat = "%0" + systemCodeLength + "d%1d%0" + exceptionCodeLength + "d";
        }

        public String getErrorCodeFormat() {
            return errorCodeFormat;
        }

        public ErrorCodeEntry parseCode(String code) {
            final int length = systemCodeLength + exceptionCodeLength + 1;
            if (code.length() != length) {
                throw new IllegalArgumentException("code: " + code + "长度不等于" + length);
            }
            ErrorCode.Level level = ErrorCode.Level.valueOf(code.charAt(systemCodeLength) - '0');
            return new ErrorCodeEntry(StrValueConverts.intConvert(code.substring(0, systemCodeLength)), level,
                    StrValueConverts.intConvert(code.substring(systemCodeLength + 1)));
        }
    }

}
