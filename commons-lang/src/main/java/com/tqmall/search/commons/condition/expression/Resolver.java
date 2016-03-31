package com.tqmall.search.commons.condition.expression;

import com.tqmall.search.commons.condition.FieldCondition;
import com.tqmall.search.commons.condition.Operator;
import com.tqmall.search.commons.exception.ResolveExpressionException;

/**
 * Created by xing on 16/3/31.
 * 单个条件解析, 通过条件对应的值判别数据类型, 通过操作符{@link Operator}创建对应的{@link FieldCondition}
 *
 * @author xing
 */
interface Resolver {

    /**
     * 是否支持resolve 该{@link Operator}
     *
     * @param op 操作类型
     * @return true 支持
     */
    boolean supportOp(Operator op);

    /**
     * @param filed    条件表达式对应的字段名
     * @param strValue 条件表达式的值部分
     * @return 具体的单个字段的条件对象
     * @throws ResolveExpressionException strValue格式错误
     */
    FieldCondition resolve(String filed, String strValue) throws ResolveExpressionException;

}
