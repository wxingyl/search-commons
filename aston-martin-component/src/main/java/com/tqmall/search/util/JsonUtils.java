package com.tqmall.search.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * json 解析类，通用于全项目
 * <p/>
 * Created by 刘一波 on 15/7/24.
 * E-Mail:yibo.liu@tqmall.com
 */
@Slf4j
public class JsonUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        objectMapper.setDateFormat(sdf);
    }

    /**
     * 提供给elasticsearch使用，把bean转换成list map 集合类型，否则不能存入索引
     *
     * @param o
     * @return
     */
    public static Object beanToJsonObject(Object o) {
        return jsonStrToList(objectToJsonStr(o), Map.class);
    }

    public static String objectToJsonStr(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (IOException e) {
            log.error("object can not objectTranslate to json", e);
        }
        return null;
    }

    public static <T> T jsonStrToObject(String json, Class<T> cls) {
        try {
            return objectMapper.readValue(json, cls);
        } catch (IOException e) {
            log.error("json cant be objectTranslate to object", e);
            return null;
        }
    }

    public static <T> T jsonDataToObject(String jsonStr, Class<T> cls) {
        if (!StringUtils.isEmpty(jsonStr)) {
            T data = JsonUtils.jsonStrToObject(jsonStr, cls);
            return data;
        } else {
            return null;
        }
    }

    public static <T> List<T> jsonStrToList(String jsonStr, Class<?> clazz) {
        List<T> list = Lists.newArrayList();
        try {
            // 指定容器结构和类型（这里是ArrayList和clazz）
            TypeFactory t = TypeFactory.defaultInstance();
            list = objectMapper.readValue(jsonStr,
                    t.constructCollectionType(ArrayList.class, clazz));
        } catch (IOException e) {
            log.error("反序列化序列化attributes，从Json到List报错", e);
        }
        return list;
    }

    public static Map jsonStrToMap(String attributes) {
        try {
            return objectMapper.readValue(attributes, HashMap.class);
        } catch (IOException e) {
            log.error("反序列化序列化attributes，从Json到HashMap报错", e);
            return new HashMap();
        }
    }


}
