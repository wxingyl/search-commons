package com.tqmall.search.commons.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Created by xing on 15/12/25.
 * Json 工具类
 */
public abstract class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * @return 读取失败返回null
     */
    public static String objToJsonStr(Object obj) {
        if (obj == null) return null;
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("json转换异常, 将" + obj.getClass() + "转化为json字符串", e);
            return null;
        }
    }

    /**
     * @return 转换失败返回null
     */
    public static <T> T jsonStrToObj(String jsonStr, Class<T> cls) {
        if (jsonStr == null) return null;
        try {
            return OBJECT_MAPPER.readValue(jsonStr, cls);
        } catch (IOException e) {
            log.warn("json转换异常, 从字符串" + jsonStr + "转化为class: " + cls, e);
            return null;
        }
    }

    /**
     * 讲json字符串转化为Map
     * @return 转换失败返回null
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonStrToMap(String jsonStr) {
        if (jsonStr == null) return null;
        try {
            return OBJECT_MAPPER.readValue(jsonStr, Map.class);
        } catch (IOException e) {
            log.warn("json转换异常, 从字符串" + jsonStr + "转化为HashMap", e);
            return null;
        }
    }
}
