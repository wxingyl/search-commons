package com.tqmall.search.db.param;

import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.Objects;

/**
 * Created by xing on 16/4/5.
 * sql语句查询, 比如count, sum等自定义查询等
 *
 * @author xing
 */
public class SqlSentenceQueryParam extends QueryParam {

    private final String sqlSentence;

    public SqlSentenceQueryParam(String schema, String table, int size, String sqlSentence) {
        super(schema, table, size);
        this.sqlSentence = SearchStringUtils.filterString(sqlSentence);
        Objects.requireNonNull(this.sqlSentence);
    }

    @Override
    protected void appendSqlStatementOfFields(StringBuilder sql) {
        sql.append(sqlSentence);
    }
}
