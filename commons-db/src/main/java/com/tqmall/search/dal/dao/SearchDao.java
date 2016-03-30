package com.tqmall.search.dal.dao;

import com.tqmall.search.dal.exception.DaoException;

import java.util.List;
import java.util.Map;

/**
 * Created by 刘一波 on 16/3/24.
 * E-Mail:yibo.liu@tqmall.com
 */
public interface SearchDao {

    List<Map<String, Object>> query(String sql) throws DaoException;

    <T> List<T> query(String sql, Class<T> bean) throws DaoException;

    <T> List<T> query(String sql, String column) throws DaoException;

    /**
     * 在创建k,v类型的缓存时可用到
     */
    <K, V> Map<K, V> query(String sql, String columnKey, String columnValue) throws DaoException;

    /**
     * 在创建k,List<v>类型的缓存时可用到
     *
     * @param ignore placeholder no use
     */
    <K, V> Map<K, List<V>> query(String sql, String columnKey, String columnValue, Void ignore) throws DaoException;

    /**
     * 在创建k,Object类型的缓存时可用到
     */
    <K, T> Map<K, T> query(String sql, Class<T> bean, String key) throws DaoException;
}
