package com.tqmall.search.commons.condition;

import com.tqmall.search.commons.lang.Function;

import java.util.Set;

/**
 * Created by xing on 16/3/29.
 * Condition 封装
 *
 * @author xing
 */
public interface Condition {

    /**
     * 条件校验
     */
    boolean verify(Function<String, String> values);

    Set<String> fields();
}
