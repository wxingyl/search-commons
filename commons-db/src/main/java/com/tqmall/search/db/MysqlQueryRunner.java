package com.tqmall.search.db;

import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Created by xing on 16/4/5.
 * mysql query runner 简单封装
 *
 * @author xing
 */
public interface MysqlQueryRunner {

    /**
     * 基本查询, 查询时发生{@link java.sql.SQLException}则返回null
     * {@link java.sql.SQLException} 在内部捕获
     *
     * @param param 参数对象
     * @param rsh   返回结果处理对象
     * @param <T>   返回模板类型
     * @return 查询时发生{@link java.sql.SQLException}则返回null
     */
    <T> T query(QueryParam param, ResultSetHandler<T> rsh);
}
