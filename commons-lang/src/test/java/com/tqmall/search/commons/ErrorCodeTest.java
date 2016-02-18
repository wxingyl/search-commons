package com.tqmall.search.commons;

import com.tqmall.search.commons.lang.ErrorCodeEntry;
import com.tqmall.search.commons.result.ErrorCode;
import com.tqmall.search.commons.utils.ErrorCodeBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/1/4.
 * ErrorCode相关测试
 */
public class ErrorCodeTest {

    @Test
    public void parseTest() {
        ErrorCodeEntry entry = ErrorCodeBuilder.Config.DEFAULT.parseCode("00010023");
        System.out.println(entry);
        entry = ErrorCodeBuilder.Config.DEFAULT.parseCode("80121023");
        System.out.println(entry);
    }

    @Test
    public void errorTestBuildTest() {
        System.out.println("系统码: 821");
        for (ErrorCodeTestEnum e : ErrorCodeTestEnum.values()) {
            System.out.println(e);
        }
        System.out.println("\n系统码: 822");
        for (ErrorCodeTest2Enum e : ErrorCodeTest2Enum.values()) {
            System.out.println(e);
        }
    }

    public enum ErrorCodeTestEnum {
        NOT_NULL_FATAL(1, "测试FATAL异常码, 不能为null"),
        ARG_INVALID_FATAL(2, "测试FATAL异常码, 参数非法"),
        NOT_NULL_ERROR(3, "测试ERROR异常码, 不能为null"),
        ARG_INVALID_ERROR(4, "测试ERROR异常码, 参数非法"),
        NOT_NULL_WARN(5, "测试WARN异常码, 不能为null"),
        ARG_INVALID_WARN(6, "测试WARN异常码, 参数非法");

        static {
            //系统码821
            ErrorCodeBuilder builder = new ErrorCodeBuilder(821);
            List<ErrorCodeTestEnum> buildList = new ArrayList<>();
            buildList.add(NOT_NULL_FATAL);
            buildList.add(ARG_INVALID_FATAL);
            for (ErrorCodeTestEnum e : buildList) {
                e.code = builder.buildFatalCode(e.exceptionCode);
            }
            buildList.clear();
            buildList.add(NOT_NULL_ERROR);
            buildList.add(ARG_INVALID_ERROR);
            for (ErrorCodeTestEnum e : buildList) {
                e.code = builder.buildErrorCode(e.exceptionCode);
            }
            buildList.clear();
            buildList.add(NOT_NULL_WARN);
            buildList.add(ARG_INVALID_WARN);
            for (ErrorCodeTestEnum e : buildList) {
                e.code = builder.buildWarnCode(e.exceptionCode);
            }
            buildList.clear();
        }

        ErrorCodeTestEnum(int exceptionCode, String message) {
            this.exceptionCode = exceptionCode;
            this.message = message;
        }

        private int exceptionCode;

        private String code;

        private String message;

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return code + ':' + message;
        }
    }

    public enum ErrorCodeTest2Enum {
        NOT_NULL_FATAL(1, "测试FATAL异常码, 不能为null", ErrorCode.Level.FATAL),
        ARG_INVALID_FATAL(2, "测试FATAL异常码, 参数非法", ErrorCode.Level.FATAL),
        NOT_NULL_ERROR(3, "测试ERROR异常码, 不能为null", ErrorCode.Level.ERROR),
        ARG_INVALID_ERROR(4, "测试ERROR异常码, 参数非法", ErrorCode.Level.ERROR),
        NOT_NULL_WARN(5, "测试WARN异常码, 不能为null", ErrorCode.Level.WARN),
        ARG_INVALID_WARN(6, "测试WARN异常码, 参数非法", ErrorCode.Level.WARN);

        static {

            //系统码822
            ErrorCodeBuilder builder = new ErrorCodeBuilder(822);
            for (ErrorCodeTest2Enum e : ErrorCodeTest2Enum.values()) {
                e.code = builder.buildCode(e.level, e.exceptionCode);
            }
        }

        ErrorCodeTest2Enum(int exceptionCode, String message, ErrorCode.Level level) {
            this.exceptionCode = exceptionCode;
            this.message = message;
            this.level = level;
        }

        private int exceptionCode;

        private ErrorCode.Level level;

        private String code;

        private String message;

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return code + ':' + message;
        }
    }

}
