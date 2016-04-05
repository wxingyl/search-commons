package com.tqmall.search.db;

/**
 * Created by xing on 16/4/5.
 * sql query 构造工具类
 *
 * @author xing
 */
public final class Queries {

    private Queries() {
    }

    public static AllColumnsQueryParam allColumns(String table) {
        return new AllColumnsQueryParam(null, table, QueryParam.DEFAULT_SIZE);
    }

    public static AllColumnsQueryParam allColumns(String schema, String table) {
        return new AllColumnsQueryParam(schema, table, QueryParam.DEFAULT_SIZE);
    }

    public static AllColumnsQueryParam allColumns(String table, int size) {
        return new AllColumnsQueryParam(null, table, size);
    }

    public static AllColumnsQueryParam allColumns(String schema, String table, int size) {
        return new AllColumnsQueryParam(schema, table, size);
    }

    public static ColumnsQueryParam columns(String table) {
        return new ColumnsQueryParam(null, table, QueryParam.DEFAULT_SIZE);
    }

    public static ColumnsQueryParam columns(String schema, String table) {
        return new ColumnsQueryParam(schema, table, QueryParam.DEFAULT_SIZE);
    }

    public static ColumnsQueryParam columns(String table, int size) {
        return new ColumnsQueryParam(null, table, size);
    }

    public static ColumnsQueryParam columns(String schema, String table, int size) {
        return new ColumnsQueryParam(schema, table, size);
    }

    public static <T> BeanQueryParam<T> bean(String table, Class<T> cls) {
        return new BeanQueryParam<>(null, table, QueryParam.DEFAULT_SIZE, cls);
    }

    public static <T> BeanQueryParam<T> bean(String schema, String table, Class<T> cls) {
        return new BeanQueryParam<>(schema, table, QueryParam.DEFAULT_SIZE, cls);
    }

    public static <T> BeanQueryParam<T> bean(String table, int size, Class<T> cls) {
        return new BeanQueryParam<>(null, table, size, cls);
    }

    public static <T> BeanQueryParam<T> bean(String schema, String table, int size, Class<T> cls) {
        return new BeanQueryParam<>(schema, table, size, cls);
    }

    public static SqlSentenceQueryParam sqlSentence(String table, String sqlSentence) {
        return new SqlSentenceQueryParam(null, table, QueryParam.DEFAULT_SIZE, sqlSentence);
    }

    public static SqlSentenceQueryParam sqlSentence(String schema, String table, int size, String sqlSentence) {
        return new SqlSentenceQueryParam(schema, table, size, sqlSentence);
    }

    /**
     * sql返回结果重名民为total
     */
    public static SqlSentenceQueryParam count(String table) {
        return new SqlSentenceQueryParam(null, table, 1, "COUNT(1) as total");
    }

    /**
     * sql返回结果重名民为total
     */
    public static SqlSentenceQueryParam count(String schema, String table) {
        return new SqlSentenceQueryParam(schema, table, 1, "COUNT(1) as total");
    }

}
