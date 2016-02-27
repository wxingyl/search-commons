package com.tqmall.search.canal;

import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.param.condition.ConditionContainer;
import com.tqmall.search.commons.param.condition.EqualCondition;
import com.tqmall.search.commons.param.condition.UnmodifiableConditionContainer;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.StrValueConverts;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by xing on 16/2/25.
 * table中基于列的刷选条件容器
 */
public class TableColumnCondition {

    /**
     * 字段名: "is_deleted"
     * 有效值: "N"
     */
    public static final EqualCondition<String> IS_DELETED_CONDITION = EqualCondition.build("is_deleted", "N");

    /**
     * 默认的逻辑删除表字段过滤器
     * 字段名: "is_deleted"
     * 有效值: "N"
     */
    public static final TableColumnCondition DEFAULT_DELETE_COLUMN_CONDITION = new TableColumnCondition(UnmodifiableConditionContainer.build()
            .addMust(IS_DELETED_CONDITION)
            .create());

    private final ConditionContainer conditionContainer;

    /**
     * 条件里面每个字段都有类型的, 比如{@link Integer}, @{@link java.math.BigDecimal}等
     * 该Map存储各个字段对应类型转化器
     *
     * @see StrValueConvert
     * @see com.tqmall.search.commons.utils.StrValueConverts
     */
    private final Map<String, StrValueConvert> columnConvertMap;

    /**
     * 不需要{@link #columnConvertMap}的条件判断容器
     */
    public TableColumnCondition(ConditionContainer conditionContainer) {
        this(conditionContainer, null);
    }

    /**
     * 推荐使用{@link #build()}构建
     *
     * @see Builder
     */
    public TableColumnCondition(ConditionContainer conditionContainer, Map<String, StrValueConvert> columnConvertMap) {
        Objects.requireNonNull(conditionContainer);
        this.conditionContainer = conditionContainer;
        if (CommonsUtils.isEmpty(columnConvertMap)) {
            this.columnConvertMap = null;
        } else {
            this.columnConvertMap = new HashMap<>(columnConvertMap);
        }
    }

    public ConditionContainer getConditionContainer() {
        return conditionContainer;
    }

    /**
     * table的单条记录验证
     *
     * @param tableRowData table 单条记录数据
     * @return 是否通过条件验证
     */
    public boolean validation(final Function<String, String> tableRowData) {
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

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {

        private ConditionContainer conditionContainer;

        private final Map<String, StrValueConvert> columnConvertMap = new HashMap<>();

        public Builder conditionContainer(ConditionContainer conditionContainer) {
            this.conditionContainer = conditionContainer;
            return this;
        }

        public <T> Builder columnConvert(String column, StrValueConvert<T> convert) {
            columnConvertMap.put(column, convert);
            return this;
        }

        public <T> Builder columnConvert(String column, Class<T> tClass) {
            columnConvertMap.put(column, StrValueConverts.getConvert(tClass));
            return this;
        }

        public TableColumnCondition create() {
            return new TableColumnCondition(conditionContainer, columnConvertMap);
        }
    }
}
