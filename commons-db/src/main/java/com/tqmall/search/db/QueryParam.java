package com.tqmall.search.db;

import com.tqmall.search.commons.condition.ConditionContainer;
import com.tqmall.search.commons.param.FieldSort;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by xing on 16/4/2.
 * 简单的mysql查询参数封装, 不支持join, group等操作
 * 必须设定pageSize, 默认20
 *
 * @author xing
 */
public abstract class QueryParam {

    /**
     * 默认的pageSize大小
     */
    public final static int DEFAULT_SIZE = 20;

    private final String schema;

    private final String table;
    /**
     * 大于0 添加的sql语句中
     */
    private final int size;

    private int start;

    private ConditionContainer queryCondition;

    private List<FieldSort> sorts;

    protected QueryParam(String schema, String table, int size) {
        Objects.requireNonNull(table);
        this.schema = schema;
        this.table = table;
        this.size = size;
    }

    protected abstract void appendSqlStatementOfFields(StringBuilder sql);

    public final QueryParam setQueryCondition(ConditionContainer queryCondition) {
        this.queryCondition = queryCondition;
        return this;
    }

    public final QueryParam addSort(FieldSort... sorts) {
        if (this.sorts == null) {
            this.sorts = new ArrayList<>();
        }
        Collections.addAll(this.sorts, sorts);
        return this;
    }

    public final QueryParam setStart(int start) {
        this.start = start;
        return this;
    }

    /**
     * 通过设定页码pageNo的方式设定start, pageNo从1开始计算
     *
     * @param pageNo 页码, 从1开始计算
     */
    public final QueryParam setPageNo(int pageNo) {
        this.start = (pageNo - 1) * size;
        return this;
    }

    /**
     * 获取mysql查询语句
     */
    public final String sqlStatement() {
        StringBuilder sql = new StringBuilder(256);
        sql.append("SELECT ");
        appendSqlStatementOfFields(sql);
        sql.append(" FROM ");
        if (schema != null) {
            SqlStatements.appendField(sql, schema).append('.');
        }
        SqlStatements.appendField(sql, table);
        if (queryCondition != null) {
            sql.append(" WHERE ");
            SqlStatements.appendContainer(sql, queryCondition);
        }
        if (!CommonsUtils.isEmpty(sorts)) {
            sql.append(" ORDER BY ");
            for (FieldSort s : sorts) {
                SqlStatements.appendField(sql, s.getField()).append(' ').append(s.isAsc() ? "ASC" : "DESC").append(", ");
            }
            sql.delete(sql.length() - 2, sql.length());
        }
        if (size > 0) {
            sql.append(" LIMIT ").append(start).append(", ").append(size);
        }
        return sql.toString();
    }

    public final String schemaTableName() {
        return schema == null ? table : schema + '.' + table;
    }

    public final String getSchema() {
        return schema;
    }

    public final int getSize() {
        return size;
    }

    public final String getTable() {
        return table;
    }

}
