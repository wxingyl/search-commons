package com.tqmall.search.util;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LinkedMap;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by 刘一波 on 16/3/29.
 * E-Mail:yibo.liu@tqmall.com
 */
public class MapUtils {

    public static final Transformer LONG_TO_INTEGER = new Transformer() {
        @Override
        public Object transform(Object input) {
            if (input != null && (input.getClass().equals(Long.TYPE) || input.getClass().equals(Long.class)))
                return ((Long) input).intValue();
            else
                return input;
        }
    };
    public static final Transformer ANY_TO_STRING = new Transformer() {
        @Override
        public Object transform(Object input) {
            return String.valueOf(input);
        }
    };

    /**
     * 把key为Long的类型转换成key为Integer
     *
     * @param map
     * @return
     */
    public static <V> Map<Integer, V> transformKeyLongToInt(Map<Long, V> map) {
        return decorateTransform(map, LONG_TO_INTEGER, null);
    }

    /**
     * 把key和value为Long的类型转换成key为Integer
     *
     * @param map
     * @return
     */
    public static Map<Integer, Integer> transformKeyValueLongToInt(Map<Long, Long> map) {
        return decorateTransform(map, LONG_TO_INTEGER, LONG_TO_INTEGER);
    }

    /**
     * 把key为Long的类型转换成key为String
     *
     * @param map
     * @return
     */
    public static <V> Map<String, V> transformKeyAnyToString(Map<?, ?> map) {
        return decorateTransform(map, ANY_TO_STRING, null);
    }

    /**
     * 由于TransformedMap.decorateTransform方法会获取原始map并putAll,在使用multiValue类时会出错,转换一次,多一层list,故用此方法代替
     */
    public static Map decorateTransform(Map map, Transformer keyTransformer, Transformer valueTransformer) {
        if (map.isEmpty()) {
            return map;
        }
        Map result = new LinkedMap(map.size());
        for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(transformKey(keyTransformer, entry.getKey()), transformValue(valueTransformer, entry.getValue()));
        }
        return result;
    }

    /**
     * Transforms a key.
     */
    protected static Object transformKey(Transformer keyTransformer, Object object) {
        if (keyTransformer == null) {
            return object;
        }
        return keyTransformer.transform(object);
    }

    /**
     * Transforms a value.
     */
    protected static Object transformValue(Transformer valueTransformer, Object object) {
        if (valueTransformer == null) {
            return object;
        }
        return valueTransformer.transform(object);
    }
}
