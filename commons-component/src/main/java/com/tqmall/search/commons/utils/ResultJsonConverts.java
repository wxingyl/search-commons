package com.tqmall.search.commons.utils;

import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.result.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 15/12/27.
 * Http调用, 以json格式的Result返回需要转换,该工具类提供转换实现
 * 返回的{@link Result}格式的json, 这儿会自动处理拿到{@link Result#data}部分
 */
public final class ResultJsonConverts {

    private ResultJsonConverts() {
    }

    /**
     * 获取{@link Result} 的String转换器
     *
     * @param cls 需要的Bean class
     * @param <T> 需要的Bean classType
     * @return Result 对应的{@link StrValueConvert}实现
     */
    public static <T> StrValueConvert<Result<T>> resultConvert(final Class<T> cls) {
        return new StrValueConvert<Result<T>>() {
            @Override
            public Result<T> convert(String input) {
                JsonSimpleResult simpleResult = parseData(input);
                if (simpleResult.isSuccess()) {
                    return ResultUtils.result(JsonUtils.parseObject(simpleResult.getData(), cls));
                } else {
                    return ResultUtils.result(simpleResult);
                }
            }
        };
    }

    public static <T> StrValueConvert<PageResult<T>> pageResultConvert(final Class<T> cls) {
        return new StrValueConvert<PageResult<T>>() {
            @Override
            public PageResult<T> convert(String input) {
                final JsonSimpleResult simpleResult = parseData(input);
                if (!simpleResult.isSuccess()) {
                    return ResultUtils.pageResult(simpleResult);
                } else if (simpleResult.flag != 2) {
                    return ResultUtils.pageResult(UtilsErrorCode.JSON_RESULT_CONVERT_INVALID_ARRAY, simpleResult.getData());
                } else {
                    List<T> list = JsonUtils.parseArray(simpleResult.getData(), cls);
                    if (list == null) {
                        return ResultUtils.pageResult(UtilsErrorCode.JSON_RESULT_CONVERT_INVALID_ARRAY, simpleResult.getData());
                    } else {
                        return ResultUtils.pageResult(list, simpleResult.total);
                    }
                }
            }
        };
    }

    private final static StrValueConvert<MapResult> MAP_RESULT_CONVERT = new StrValueConvert<MapResult>() {
        @SuppressWarnings({"rawstype", "unchecked"})
        @Override
        public MapResult convert(String input) {
            final JsonSimpleResult simpleResult = parseData(input);
            if (!simpleResult.isSuccess()) {
                return ResultUtils.mapResult(simpleResult);
            } else if (simpleResult.flag != 1) {
                return ResultUtils.mapResult(UtilsErrorCode.JSON_RESULT_CONVERT_INVALID_OBJECT, simpleResult.getData());
            } else {
                MapResult result = ResultUtils.mapResult();
                Map<String, Object> map = JsonUtils.parseToMap(simpleResult.getData());
                result.putAll(map);
                return result;
            }
        }
    };

    public static StrValueConvert<MapResult> mapResultConvert() {
        return MAP_RESULT_CONVERT;
    }

    /**
     * @param jsonArray jsonArray.charAt(0) == '[', 该条件必须满足
     * @return 将json数组字符串分隔
     */
    public static List<String> splitJsonArray(String jsonArray) {
        int lastIndex = jsonArray.length() - 1;
        if (jsonArray.charAt(0) != '[' || jsonArray.charAt(lastIndex) != ']') return null;
        List<String> retList = new ArrayList<>();
        char startCh = 0, endCh = 0;
        int startIndex = 0, deep = 0;
        for (int i = 1; i < lastIndex; i++) {
            char ch = jsonArray.charAt(i);
            if (startCh == 0 && (ch == '[' || ch == '{')) {
                startCh = ch;
                startIndex = i;
                endCh = ch == '[' ? ']' : '}';
                deep++;
            } else if (startCh == ch) {
                deep++;
            } else if (endCh == ch) {
                deep--;
                if (deep == 0) {
                    retList.add(jsonArray.substring(startIndex, ++i));
                }
            }
        }
        if (startCh == 0) {
            Collections.addAll(retList, SearchStringUtils.split(jsonArray.substring(1, jsonArray.length() - 1), ','));
        }
        return retList;
    }

    private static JsonSimpleResult buildErrorSimpleResult(String message) {
        return ResultUtils.wrapError(UtilsErrorCode.JSON_RESULT_PARSE_INVALID_STRING, new ResultUtils.ResultBuild<JsonSimpleResult>() {
            @Override
            public JsonSimpleResult errorBuild(ErrorCode errorCode) {
                return new JsonSimpleResult(errorCode);
            }
        }, message);
    }

    /**
     * @param json json, 并且符合Result返回格式的字符串
     * @return not null
     */
    private static JsonSimpleResult parseData(String json) {
        if (SearchStringUtils.isEmpty(json)) {
            return buildErrorSimpleResult("json string is empty");
        }
        int dataIndex = json.indexOf("\"data\"");
        if (dataIndex < 0) {
            return buildErrorSimpleResult("can not find data field");
        }
        int i = dataIndex - 1;
        //找data前面的位置
        for (; i > 0; i--) {
            if (',' == json.charAt(i)) break;
        }
        //这儿能够处理数组格式,但是对于单个对象的就无能为力了,比如Result<String>类型, 这儿需要做特殊处理
        final int startIndex = i;
        String dataValue = null;
        String simpleJson = null;
        int flag = 0;
        dataIndex += 7;
        char startCh = 0, endCh = 0;
        int startValueIndex = -1;
        final int length = json.length();
        int deep = 0;
        for (i = dataIndex; i < length; i++) {
            char ch = json.charAt(i);
            if (startCh == 0 && (ch == '{' || ch == '[')) {
                startCh = ch;
                flag = ch == '[' ? 2 : 1;
                endCh = flag == 2 ? ']' : '}';
                deep++;
                startValueIndex = i;
            } else if (ch == startCh) {
                deep++;
            } else if (ch == endCh) {
                deep--;
                if (deep == 0) {
                    dataValue = json.substring(startValueIndex, ++i);
                    simpleJson = json.substring(0, startIndex) + json.substring(i);
                    break;
                }
            }
        }
        if (simpleJson == null) {
            simpleJson = json;
        }
        JsonSimpleResult simpleResult = JsonUtils.parseObject(simpleJson, JsonSimpleResult.class);
        if (simpleResult == null) {
            return buildErrorSimpleResult("String: " + simpleJson + " is not format of " + Result.class);
        }
        if (dataValue != null) {
            simpleResult.setData(dataValue);
        }
        simpleResult.flag = flag;
        return simpleResult;
    }

    /**
     * 该Bean从Json返回串解析并不是解析所有字段, 只解析total, code, message, success 4个fields, data是在后面程序自己搞进去的
     */
    @SuppressWarnings("serial")
    private final static class JsonSimpleResult extends Result<String> implements ErrorCode {

        private long total;

        /**
         * 0: 异常类型, 返回结果为String或者null
         * 1: 普通的Object类型
         * 2: 普通的Array类型
         */
        private int flag;

        public JsonSimpleResult() {
            super();
        }

        public JsonSimpleResult(ErrorCode errorCode) {
            super(errorCode);
        }

        @Override
        public void setCode(String code) {
            super.setCode(code);
        }

        @Override
        public void setMessage(String message) {
            super.setMessage(message);
        }

        public void setTotal(long total) {
            this.total = total;
        }

        @Override
        public void setSuccess(boolean success) {
            super.setSuccess(success);
        }

        //仅仅是为了兼容一些接口返回success 写成 succeed
        public void setSucceed(boolean succeed) {
            super.setSuccess(succeed);
        }

        @Override
        public void setData(String data) {
            super.setData(data);
        }

    }
}
