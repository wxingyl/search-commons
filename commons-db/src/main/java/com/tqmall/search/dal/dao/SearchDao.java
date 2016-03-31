package com.tqmall.search.dal.dao;

import com.tqmall.search.dal.exception.DaoException;

import java.util.List;
import java.util.Map;

/**
 * 此接口所有参数不同只为了能够返回不同的结果.
 * 1. 带keyed的接口返回的是map,参数中的字符串为key.
 * 2. value代表查询一个列的值
 * 3. Map代表返回结果是一个map
 * 4. List代表返回结果是List集合
 * 5. MapList代表返回结果是一个list,其中内容是map
 * 6. BeanList代表返回结果是一个list,其中内容是bean
 * 7. ValueList代表返回结果是一个list,其中内容是一个列的值
 * 8. Bean代表返回结果是Bean
 * <p/>
 * Created by 刘一波 on 16/3/24.
 * E-Mail:yibo.liu@tqmall.com
 */
public interface SearchDao {

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> queryMap(String sql) throws DaoException;

    @SuppressWarnings("unchecked")
    <T> List<T> queryBean(String sql, Class<T> bean) throws DaoException;

    @SuppressWarnings("unchecked")
    <T> List<T> queryValue(String sql, String column) throws DaoException;

    @SuppressWarnings("unchecked")
    <K, V> Map<K, V> queryKeyedValue(String sql, String columnKey, String columnValue) throws DaoException;

    @SuppressWarnings("unchecked")
    <K, V> Map<K, List<V>> queryKeyedValueList(String sql, String columnKey, String columnValue) throws DaoException;

    @SuppressWarnings("unchecked")
    <K, T> Map<K, T> queryKeyedBean(String sql, Class<T> bean, String key) throws DaoException;

    @SuppressWarnings("unchecked")
    <K, T> Map<K, List<T>> queryKeyedBeanList(String sql, Class<T> bean, String key) throws DaoException;

    @SuppressWarnings("unchecked")
    <K> Map<K, List<Map>> queryKeyedMapList(String sql, String key) throws DaoException;

    @SuppressWarnings("unchecked")
    <K> Map<K, Map> queryKeyedMap(String sql, String key) throws DaoException;
}
