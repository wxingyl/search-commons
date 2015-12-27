package com.tqmall.search.common.utils;

import com.google.common.collect.Lists;
import com.tqmall.search.common.result.*;

import java.util.List;
import java.util.Map;

/**
 * Created by xing on 15/12/27.
 * Http调用, 以json格式的Result返回需要转换,该工具类提供转换实现
 */
public abstract class ResultJsonConverts {

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
                if (simpleResult.isSucceed()) {
                    return ResultUtils.result(JsonUtils.jsonStrToObj(input, cls));
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
                if (!simpleResult.succeed) {
                    return ResultUtils.pageResult(simpleResult);
                } else if (!simpleResult.isArray) {
                    return ResultUtils.pageResult(UtilsErrorCode.JSON_RESULT_CONVERT_INVALID_ARRAY, simpleResult.data);
                } else {
                    List<String> list = splitJsonArray(simpleResult.data);
                    if (list == null) {
                        return ResultUtils.pageResult(UtilsErrorCode.JSON_RESULT_CONVERT_INVALID_ARRAY, simpleResult.data);
                    } else {
                        List<T> beanList = Lists.newArrayListWithExpectedSize(list.size());
                        for (String s : list) {
                            beanList.add(JsonUtils.jsonStrToObj(s, cls));
                        }
                        return ResultUtils.pageResult(beanList, simpleResult.total);
                    }
                }
            }
        };
    }

    private final static StrValueConvert<MapResult> MAP_RESULT_CONVERT = new StrValueConvert<MapResult>() {
        @SuppressWarnings("unchecked")
        @Override
        public MapResult convert(String input) {
            final JsonSimpleResult simpleResult = parseData(input);
            if (!simpleResult.succeed) {
                return ResultUtils.mapResult(simpleResult);
            } else if (simpleResult.isArray) {
                return ResultUtils.mapResult(UtilsErrorCode.JSON_RESULT_CONVERT_INVALID_OBJECT, simpleResult.data);
            } else {
                MapResult result = ResultUtils.mapResult();
                Map<String, Object> map = JsonUtils.jsonStrToObj(simpleResult.data, Map.class);
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
        List<String> retList = Lists.newArrayList();
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
        return retList;
    }

    public static JsonSimpleResult parseData(String json) {
        int dataIndex = json.indexOf("\"data\"");
        if (dataIndex < 0) {
            throw new ResultJsonParseException("Can not find data field");
        }
        int i = dataIndex;
        String dataValue = null;
        String simpleJson = null;
        boolean isArray = false;
        while (--i > 0) {
            //找data前面的位置
            if (',' == json.charAt(i)) {
                final int startIndex = i;
                dataIndex += 7;
                char startCh = 0, endCh = 0;
                int startValueIndex = -1;
                final int length = json.length();
                int deep = 0;
                for (i = dataIndex; i < length; i++) {
                    char ch = json.charAt(i);
                    if (startCh == 0 && (ch == '{' || ch == '[')) {
                        startCh = ch;
                        isArray = ch == '[';
                        endCh = isArray ? ']' : '}';
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
                break;
            }
        }
        if (dataValue == null) {
            throw new ResultJsonParseException("Can not found right data field string value from json string: " + json);
        }
        JsonSimpleResult simpleResult = JsonUtils.jsonStrToObj(simpleJson, JsonSimpleResult.class);
        if (simpleResult == null) {
            throw new ResultJsonParseException("string: " + simpleJson + " is not format of com.tqmall.search.common.result.Result class");
        }
        simpleResult.setData(dataValue, isArray);
        return simpleResult;
    }

    public final static class JsonSimpleResult implements ErrorCode {

        private String code;

        private String message;

        private long total;

        private boolean succeed;

        private String data;

        private boolean isArray;

        public void setCode(String code) {
            this.code = code;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public void setSucceed(boolean succeed) {
            this.succeed = succeed;
        }

        public void setData(String data, boolean isArray) {
            this.data = data;
            this.isArray = isArray;
        }

        @Override
        public String getCode() {
            return code;
        }

        public String getData() {
            return data;
        }

        public boolean isArray() {
            return isArray;
        }

        @Override
        public String getMessage() {
            return message;
        }

        public long getTotal() {
            return total;
        }

        public boolean isSucceed() {
            return succeed;
        }
    }
}
