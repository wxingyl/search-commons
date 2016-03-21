package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.match.Hit;

/**
 * Created by xing on 16/3/21.
 *
 * @author xing
 */
public class Utils {

    public static <V> Hit<V> hitValueOf(int start, String key, V value) {
        return new Hit<>(start, start + key.length(), value);
    }
}
