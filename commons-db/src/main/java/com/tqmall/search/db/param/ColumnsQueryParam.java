package com.tqmall.search.db.param;

import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by xing on 16/4/5.
 * 指定字段的query
 *
 * @author xing
 */
public class ColumnsQueryParam extends QueryParam {

    private final Set<String> columns = new HashSet<>();

    public ColumnsQueryParam(String schema, String table, int size) {
        super(schema, table, size);
    }

    public ColumnsQueryParam addColumn(String column) {
        columns.add(column);
        return this;
    }

    public ColumnsQueryParam addColumn(String... columns) {
        Collections.addAll(this.columns, columns);
        return this;
    }

    public ColumnsQueryParam addColumn(Collection<String> columns) {
        this.columns.addAll(columns);
        return this;
    }

    @Override
    protected void appendSqlStatementOfFields(StringBuilder sql) {
        if (columns.isEmpty()) {
            throw new IllegalArgumentException(schemaTableName() + " columns is empty");
        }
        sql.append(SearchStringUtils.join(columns, ','));
    }
}
