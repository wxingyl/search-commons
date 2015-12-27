package com.tqmall.search.common.result;

import java.util.Collection;

/**
 * Created by xing on 15/12/13.
 * 构建Result 工具类
 */
public final class ResultUtils {

    private ResultUtils() {
    }

    private static final ResultBuild<Result> RESULT_BUILD = new ResultBuild<Result>() {

        @Override
        public Result errorBuild(ErrorCode errorCode) {
            return new Result(errorCode);
        }
    };

    private static final ResultBuild<MapResult> MAP_RESULT_BUILD = new ResultBuild<MapResult>() {

        @Override
        public MapResult errorBuild(ErrorCode errorCode) {
            return new MapResult(errorCode);
        }
    };

    private static final ResultBuild<PageResult> PAGE_RESULT_BUILD = new ResultBuild<PageResult>() {

        @Override
        public PageResult errorBuild(ErrorCode errorCode) {
            return new PageResult(errorCode);
        }
    };

    public static <T> Result<T> result(T data) {
        return new Result<>(data);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> result(ErrorCode errorCode) {
        return wrapError(errorCode, RESULT_BUILD);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> result(ErrorCode errorCode, Object... args) {
        return wrapError(errorCode, RESULT_BUILD, args);
    }

    public static <T> PageResult<T> pageResult(Collection<T> data, long total) {
        return new PageResult<>(data, total);
    }

    @SuppressWarnings("unchecked")
    public static <T> PageResult<T> pageResult(ErrorCode errorCode) {
        return wrapError(errorCode, PAGE_RESULT_BUILD);
    }

    @SuppressWarnings("unchecked")
    public static <T> PageResult<T> pageResult(ErrorCode errorCode, Object... args) {
        return wrapError(errorCode, PAGE_RESULT_BUILD, args);
    }

    public static MapResult mapResult() {
        return new MapResult();
    }

    public static MapResult mapResult(String key, Object val) {
        MapResult mapResult = new MapResult();
        mapResult.put(key, val);
        return mapResult;
    }

    public static MapResult mapResult(ErrorCode errorCode) {
        return wrapError(errorCode, MAP_RESULT_BUILD);
    }

    public static MapResult mapResult(ErrorCode errorCode, Object... args) {
        return wrapError(errorCode, MAP_RESULT_BUILD, args);
    }

    /**
     * 留给外部接口自定义使用
     * @param errorCode 具体的errorCode
     * @param build Result构造自定义
     * @param <T> 具体的Result类型
     * @return 构造的Result
     */
    public static <T extends Result> T wrapError(ErrorCode errorCode, ResultBuild<T> build) {
        return build.errorBuild(errorCode);
    }

    public static <T extends Result> T wrapError(final ErrorCode errorCode, ResultBuild<T> build, Object... args) {
        if (args.length == 0) {
            return build.errorBuild(errorCode);
        } else {
            final String message = String.format(errorCode.getMessage(), args);
            return build.errorBuild(new ErrorCode() {
                @Override
                public String getCode() {
                    return errorCode.getCode();
                }

                @Override
                public String getMessage() {
                    return message;
                }
            });
        }
    }

    interface ResultBuild<T extends Result> {

        T errorBuild(ErrorCode errorCode);

    }

}
