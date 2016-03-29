package com.tqmall.search.commons.param.condition;

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
    boolean validation(Function<String, String> values);

    Set<String> fields();

    /**
     * 条件类型
     */
    enum Type {
        //且关系
        MUST,
        //或关系
        SHOULD,
        //非关系
        MUST_NOT
    }
}
