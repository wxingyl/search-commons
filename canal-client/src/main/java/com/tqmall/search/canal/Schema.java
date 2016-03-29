package com.tqmall.search.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.common.collect.Iterators;
import com.tqmall.search.canal.action.Actionable;
import com.tqmall.search.commons.param.condition.ConditionContainer;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.*;

/**
 * Created by xing on 16/2/26.
 * schema 对象封装, 为了保证table在创建完成之后不可修改, 做了只能通过提供的静态方法构造的限制
 * <p/>
 * {@link #iterator()}不可修改的, 即{@link Iterator#remove()}操纵不支持
 *
 * @see Schemas#buildSchema(String, Class)
 * @see Schemas.Builder
 * @see Schemas#buildTable(String)
 */
public class Schema<T extends Actionable> implements Iterable<Schema<T>.Table> {

    private final String schemaName;

    /**
     * key tableName
     */
    private final Map<String, Table> tableMap = new HashMap<>();

    /**
     * 为了保证table在创建完成之后不可修改, 做了只能通过提供的静态方法构造的限制
     *
     * @see Schemas#buildSchema(String, Class)
     * @see Schemas#buildTable(String)
     */
    Schema(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public Table getTable(String tableName) {
        return tableMap.get(tableName);
    }

    /**
     * 获取所有table集合, 不可更改
     */
    public Collection<Table> tables() {
        return Collections.unmodifiableCollection(tableMap.values());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Schema)) return false;

        Schema<?> schema = (Schema<?>) o;

        return schemaName.equals(schema.schemaName);
    }

    @Override
    public int hashCode() {
        return schemaName.hashCode();
    }

    /**
     * {@link Iterator#remove()}操纵不支持
     */
    @Override
    public Iterator<Table> iterator() {
        return Iterators.unmodifiableIterator(tableMap.values().iterator());
    }

    /**
     * 创建一个Table对象
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    Schema<T> addTable(Schemas.TableBuilder builder) {
        tableMap.put(builder.tableName, new Table(builder.tableName, (T) builder.action,
                builder.columns, builder.columnCondition, builder.forbidEventType));
        return this;
    }

    public class Table {

        private final String tableName;

        /**
         * 该表中感兴趣的列, 不指定默认不做过滤
         * 建议设置上该值, 防止大量无关数据更新拖累
         */
        private final Set<String> columns;
        /**
         * 该表对应事件
         */
        private final T action;

        /**
         * 列条件筛选容器
         */
        private final ConditionContainer columnCondition;
        /**
         * 排除的事件类型, 目前只支持{@link CanalEntry.EventType#UPDATE}, {@link CanalEntry.EventType#DELETE}, {@link CanalEntry.EventType#INSERT}
         */
        private final byte forbidEventType;

        Table(String tableName, T action, Collection<String> columns, ConditionContainer columnCondition, byte forbidEventType) {
            Objects.requireNonNull(action);
            Objects.requireNonNull(tableName);
            this.tableName = tableName;
            this.action = action;
            this.columnCondition = columnCondition;
            if (!CommonsUtils.isEmpty(columns)) {
                Set<String> columnSet = new HashSet<>(columns);
                if (columnCondition != null) {
                    /**
                     * 要保证在判断条件中的column添加到{@link #columns}
                     */
                    columnSet.addAll(columnCondition.fields());
                }
                this.columns = Collections.unmodifiableSet(columnSet);
            } else this.columns = null;
            if ((forbidEventType & 7) == 7) {
                throw new IllegalArgumentException("forbidEventType: " + Integer.toBinaryString(forbidEventType)
                        + " should not contain all types of UPDATE, INSERT, DELETE");
            }
            this.forbidEventType = forbidEventType;
        }

        public final String getSchemaName() {
            return schemaName;
        }

        public final String getTableName() {
            return tableName;
        }

        public final T getAction() {
            return action;
        }

        /**
         * unmodifiableSet, 如果没有过滤字段, 或者{@link #columns} isEmpty() 为true, 则返回null,
         * 即返回结果不为null肯定存在感兴趣column
         *
         * @see Collections#unmodifiableSet(Set)
         */
        public final Set<String> getColumns() {
            return columns;
        }

        public final ConditionContainer getColumnCondition() {
            return columnCondition;
        }

        public final byte getForbidEventType() {
            return forbidEventType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Schema.Table)) return false;

            Schema.Table table = (Schema.Table) o;

            return tableName.equals(table.tableName) && schemaName.equals(table.getSchemaName());
        }

        @Override
        public int hashCode() {
            return 31 * tableName.hashCode() + schemaName.hashCode();
        }

        @Override
        public String toString() {
            return schemaName + '.' + tableName + (columns == null ? "" : ',' + SearchStringUtils.join(columns, ','));
        }
    }

}
