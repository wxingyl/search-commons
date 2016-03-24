package com.tqmall.search.commons.utils;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by xing on 15/12/25.
 * Json 工具类
 */
public abstract class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    /**
     * @return 读取失败返回null
     */
    public static String toJsonStr(Object obj) {
        if (obj == null) return null;
        try {
            return JSON.toJSONString(obj);
        } catch (RuntimeException e) {
            log.warn("json转换异常, 将" + obj.getClass() + "转化为json字符串", e);
            return null;
        }
    }

    public static <T> T parseObject(String text, Class<T> cls) {
        try {
            return JSON.parseObject(text, cls);
        } catch (RuntimeException e) {
            log.warn("json转换异常, 从字符串" + text + "转化为Object: " + cls, e);
            return null;
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> cls) {
        try {
            return JSON.parseArray(text, cls);
        } catch (RuntimeException e) {
            log.warn("json转换异常, 从字符串" + text + "转化为数组: " + cls, e);
            return null;
        }
    }

    /**
     * 讲json字符串转化为Map
     *
     * @return 转换失败返回null
     */
    public static Map<String, Object> parseToMap(String text) {
        if (text == null) return null;
        try {
            return JSON.parseObject(text);
        } catch (RuntimeException e) {
            log.warn("json转换异常, 从字符串" + text + "转化为Map", e);
            return null;
        }
    }
}
