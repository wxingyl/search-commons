package com.tqmall.search.canal.action;

import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.param.condition.ConditionContainer;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xing on 16/2/25.
 * table中基于列的刷选条件容器
 */
public class TableColumnCondition {

    private final ConditionContainer conditionContainer;

    /**
     * 条件里面每个字段都有类型的, 比如{@link Integer}, @{@link java.math.BigDecimal}等
     * 该Map存储各个字段对应类型转化器
     *
     * @see StrValueConvert
     * @see com.tqmall.search.commons.utils.StrValueConverts
     */
    private final Map<String, StrValueConvert> columnConvertMap;

    public TableColumnCondition(ConditionContainer conditionContainer, Map<String, StrValueConvert> columnConvertMap) {
        this.conditionContainer = conditionContainer;
        if (CommonsUtils.isEmpty(columnConvertMap)) {
            this.columnConvertMap = new HashMap<>(columnConvertMap);
        } else {
            this.columnConvertMap = null;
        }
    }

    /**
     * table的单条记录验证
     *
     * @param tableRowData table 单条记录数据
     * @return 是否通过条件验证
     */
    public boolean vaildation(final Function<String, String> tableRowData) {
        if (CommonsUtils.isEmpty(columnConvertMap)) {
            return conditionContainer.validation(tableRowData);
        } else {
            return conditionContainer.validation(new Function<String, Object>() {
                @Override
                public Object apply(String column) {
                    StrValueConvert convert = columnConvertMap.get(column);
                    //如果没有对应的Convert, 则使用字符串类型
                    if (convert == null) return tableRowData.apply(column);
                    return convert.convert(tableRowData.apply(column));
                }
            });
        }

    }
}
