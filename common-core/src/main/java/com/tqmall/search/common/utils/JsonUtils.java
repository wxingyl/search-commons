package com.tqmall.search.common.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by xing on 15/12/25.
 * Json 工具类
 */
public abstract class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String objToJsonStr(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("json转换异常, 将" + obj.getClass() + "转化为json字符串", e);
            return null;
        }
    }

    public static <T> T jsonStrToObj(String jsonStr, Class<T> cls) {
        try {
            return objectMapper.readValue(jsonStr, cls);
        } catch (IOException e) {
            log.warn("json转换异常, 从字符串" + jsonStr + "转化为class: " + cls, e);
            return null;
        }
    }
}
