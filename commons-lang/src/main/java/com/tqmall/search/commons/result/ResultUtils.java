package com.tqmall.search.commons.result;

import java.util.Collection;
import java.util.Objects;

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


    /**
     * errorCode与result比较code值是否相同
     *
     * @see Result#isEquals(ErrorCode)
     */
    public static boolean equals(Result<?> result, ErrorCode errorCode) {
        return Objects.equals(result.getCode(), errorCode.getCode());
    }

    /**
     * 将一个Result对应输出
     */
    public static String resultToString(Result result) {
        return "Result: succeed = " + result.isSuccess() + ", code = " + result.getCode() + ", message = " + result.getMessage()
                + (result instanceof PageResult ? (", total = " + ((PageResult) result).getTotal()) : "")
                + ", data = " + result.getData();
    }

    /**
     * 构建成功的, 拥有返回数据的{@link Result}对象
     *
     * @param data 返回数据
     * @param <T>  返回数据对应的类型
     * @return 成功的, 拥有返回数据的{@link Result}对象
     */
    public static <T> Result<T> result(T data) {
        return new Result<>(data);
    }

    /**
     * 构建错误的{@link Result}对象
     *
     * @param errorCode 错误码
     * @param <T>       没有意义, 只是为了调用的时候少点警告而已
     * @return 错误的{@link Result}对象
     */
    @SuppressWarnings({"rawstype", "unchecked"})
    public static <T> Result<T> result(ErrorCode errorCode) {
        return wrapError(errorCode, RESULT_BUILD);
    }

    /**
     * 构建错误的{@link Result}对象, 错误Message中有参数
     *
     * @param errorCode 错误码
     * @param <T>       没有意义, 只是为了调用的时候少点警告而已
     * @return 错误的{@link Result}对象
     */
    @SuppressWarnings({"rawstype", "unchecked"})
    public static <T> Result<T> result(ErrorCode errorCode, Object... args) {
        return wrapError(errorCode, RESULT_BUILD, args);
    }

    /**
     * @return PageResult类型
     * @see #result(Object)
     */
    public static <T> PageResult<T> pageResult(Collection<T> data, long total) {
        return new PageResult<>(data, total);
    }

    /**
     * @return PageResult类型
     * @see #result(ErrorCode)
     */
    @SuppressWarnings({"rawstype", "unchecked"})
    public static <T> PageResult<T> pageResult(ErrorCode errorCode) {
        return wrapError(errorCode, PAGE_RESULT_BUILD);
    }

    /**
     * @return PageResult类型
     * @see #result(ErrorCode, Object...)
     */
    @SuppressWarnings({"rawstype", "unchecked"})
    public static <T> PageResult<T> pageResult(ErrorCode errorCode, Object... args) {
        return wrapError(errorCode, PAGE_RESULT_BUILD, args);
    }

    /**
     * @return MapResult类型
     * @see #result(Object)
     */
    public static MapResult mapResult() {
        return new MapResult();
    }

    /**
     * @return MapResult类型
     * @see #result(Object)
     */
    public static MapResult mapResult(String key, Object val) {
        MapResult mapResult = new MapResult();
        mapResult.put(key, val);
        return mapResult;
    }

    /**
     * @return MapResult类型
     * @see #result(ErrorCode)
     */
    public static MapResult mapResult(ErrorCode errorCode) {
        return wrapError(errorCode, MAP_RESULT_BUILD);
    }

    /**
     * @return MapResult类型
     * @see #result(ErrorCode, Object...)
     */
    public static MapResult mapResult(ErrorCode errorCode, Object... args) {
        return wrapError(errorCode, MAP_RESULT_BUILD, args);
    }

    /**
     * 留给外部接口自定义使用
     *
     * @param errorCode 具体的errorCode
     * @param build     Result构造自定义
     * @param <T>       具体的Result类型
     * @return 构造的Result
     */
    public static <T extends Result> T wrapError(ErrorCode errorCode, ResultBuild<T> build) {
        return build.errorBuild(errorCode);
    }

    /**
     * error message中带有参数的构造
     *
     * @see #wrapError(ErrorCode, ResultBuild)
     */
    public static <T extends Result> T wrapError(ErrorCode errorCode, ResultBuild<T> build, Object... args) {
        return build.errorBuild(wrapperErrorCode(errorCode, args));
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

    /**
     * 构造Result返回类型接口定义, 可以自定义Result类型
     *
     * @param <T> 具体的Result类型
     */
    public interface ResultBuild<T extends Result> {

        T errorBuild(ErrorCode errorCode);

    }

}
