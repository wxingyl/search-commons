package com.tqmall.search.db;

import com.tqmall.search.db.param.BeanQueryParam;
import com.tqmall.search.db.param.QueryParam;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.BeanMapHandler;

import java.util.List;
import java.util.Map;

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

    //下面的三个接口是跟bean相关的查询, 其实跟调用query(QueryParam param, ResultSetHandler<T> rsh)没有什么差, 只不过为了保证BeanQueryParam
    // 的泛型跟BeanHandle的bean同一个类型就加了下面的接口

    /**
     * 单个bean query
     *
     * @param handler bean处理器, 通过{@link Queries#beanHandler(Class)}获取
     * @param <T>     bean class type
     * @return bean
     * @see Queries#beanHandler(Class)
     */
    <T> T query(BeanQueryParam<T> param, BeanHandler<T> handler);

    /**
     * List<Bean> query
     *
     * @param handler bean处理器, 通过{@link Queries#beanListHandler(Class)}获取
     * @param <T>     bean class type
     * @return bean list
     * @see Queries#beanListHandler(Class)
     */
    <T> List<T> query(BeanQueryParam<T> param, BeanListHandler<T> handler);

    /**
     * Map<K, Bean> query
     *
     * @param handler bean处理器, 通过{@link Queries#beanMapHandler(Class)}, {@link Queries#beanMapHandler(Class, int)},
     *                {@link Queries#beanMapHandler(Class, String)}获取
     * @param <K>     Map的key
     * @param <V>     bean class type
     * @see Queries#beanMapHandler(Class)
     * @see Queries#beanMapHandler(Class, int)
     * @see Queries#beanMapHandler(Class, String)
     */
    <K, V> Map<K, V> query(BeanQueryParam<V> param, BeanMapHandler<K, V> handler);
}
