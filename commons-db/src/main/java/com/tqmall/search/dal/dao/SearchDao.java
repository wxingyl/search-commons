package com.tqmall.search.dal.dao;

import com.tqmall.search.dal.exception.DaoException;

import java.util.List;
import java.util.Map;

/**
 * Created by 刘一波 on 16/3/24.
 * E-Mail:yibo.liu@tqmall.com
 */
public interface SearchDao {
    /**
     * 把数据库中的一行以List<map>形式返回
     */
    List<Map<String, Object>> query(String sql) throws DaoException;

    /**
     * 把数据库中的一行以List<bean>形式返回
     */
    <T> List<T> query(String sql, Class<T> bean) throws DaoException;

    /**
     * 把数据库中的一行中的某一列以list<Object>形式返回
     */
    <T> List<T> query(String sql, String column) throws DaoException;

    /**
     * 在创建k,v类型的结果时可用到
     */
    <K, V> Map<K, V> query(String sql, String columnKey, String columnValue) throws DaoException;

    /**
     * 在创建k,List<v>类型的结果时可用到
     *
     * @param ignore placeholder no use
     */
    <K, V> Map<K, List<V>> query(String sql, String columnKey, String columnValue, Void ignore) throws DaoException;

    /**
     * 在创建k,Object类型的结果时可用到
     */
    <K, T> Map<K, T> query(String sql, Class<T> bean, String key) throws DaoException;

    /**
     * 在创建k,List<Object>类型的结果时可用到
     */
    @SuppressWarnings("unchecked")
    <K, T> Map<K, List<T>> query(String sql, Class<T> bean, String key, Void ignore) throws DaoException;
}
