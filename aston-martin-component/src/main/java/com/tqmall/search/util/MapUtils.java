package com.tqmall.search.util;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.TransformedMap;

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
        return TransformedMap.decorateTransform(map, LONG_TO_INTEGER, null);
    }

    /**
     * 把key和value为Long的类型转换成key为Integer
     *
     * @param map
     * @return
     */
    public static Map<Integer, Integer> transformKeyValueLongToInt(Map<Long, Long> map) {
        return TransformedMap.decorateTransform(map, LONG_TO_INTEGER, LONG_TO_INTEGER);
    }

    /**
     * 把key为Long的类型转换成key为Integer
     *
     * @param map
     * @return
     */
    public static <V> Map<String, V> transformKeyAnyToString(Map<?,?> map) {
        return TransformedMap.decorateTransform(map, ANY_TO_STRING, null);
    }

}
