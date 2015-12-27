package com.tqmall.search.common.utils;

import com.tqmall.search.common.result.ErrorCode;
import com.tqmall.search.common.result.Result;
import com.tqmall.search.common.result.ResultUtils;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by xing on 15/12/27.
 * Http调用, 以json格式的Result返回
 */
public class ResultJsonConvert<T> implements StrValueConvert<Result<T>> {

    private Class<T> cls;

    protected final static ErrorCode JSON_PARSE_ERROR = new ErrorCode() {
        @Override
        public String getCode() {
            return "0";
        }

        @Override
        public String getMessage() {
            return "Json解析错误";
        }
    };

    public ResultJsonConvert(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public Result<T> convert(String input) {
        JSONObject jsonObject = new JSONObject(input);


        final JsonParseResult result = JsonUtils.jsonStrToObj(input, JsonParseResult.class);
        if (!jsonObject.getBoolean("succeed")) {
            return ResultUtils.result(JSON_PARSE_ERROR);
        }
        if (Boolean.TRUE.equals(result.succeed)) {
            return ResultUtils.result(JsonUtils.jsonStrToObj(result.data.get(0), cls));
        } else {
            return ResultUtils.result(new ErrorCode() {
                @Override
                public String getCode() {
                    return result.code;
                }

                @Override
                public String getMessage() {
                    return result.message;
                }
            });
        }
    }

    public static class JsonParseResult {

        private Boolean succeed;

        private String code;

        private String message;

        private List<String> data;

        public void setCode(String code) {
            this.code = code;
        }

        public void setData(List<String> data) {
            this.data = data;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setSucceed(Boolean succeed) {
            this.succeed = succeed;
        }

        public String getCode() {
            return code;
        }

        public List<String> getData() {
            return data;
        }

        public String getMessage() {
            return message;
        }

        public Boolean getSucceed() {
            return succeed;
        }
    }
}
