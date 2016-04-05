package com.tqmall.search.db;

/**
 * Created by xing on 16/4/5.
 * 查询是直接查询所有列, 即"SELECT * FROM XXX"
 * @author xing
 */
public class AllColumnsQueryParam extends QueryParam {

    public AllColumnsQueryParam(String schema, String table, int size) {
        super(schema, table, size);
    }

    @Override
    protected void appendSqlStatementOfFields(StringBuilder sql) {
        sql.append('*');
    }
}
