package com.tqmall.search.dal.dao;

import com.tqmall.search.dal.exception.DaoException;
import com.tqmall.search.dal.processor.*;
import org.apache.commons.dbutils.*;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * Created by 刘一波 on 16/3/23.
 * E-Mail:yibo.liu@tqmall.com
 */
public class BaseDao implements SearchDao {

    final Logger log = LoggerFactory.getLogger(getClass());

    //是否需要微秒,以前的接口都是不需要的,所以暂时不需要
    private boolean needMillisecond = false;

    private QueryRunner queryRunner;
    private static final ResultSetHandler mapListHandler = new MapListHandler();
    private static final RowProcessor generousBeanRowProcessor = new BasicRowProcessor(new GenerousBeanProcessor());
    private static final RowProcessor noMillisecondGenerousBeanRowProcessor = new BasicRowProcessor(new NoMillisecondGenerousBeanProcessor());

    public BaseDao(DataSource dataSource) {
        queryRunner = new QueryRunner(dataSource);
    }

    private RowProcessor getBeanRowProcessor() {
        if (needMillisecond) {
            return generousBeanRowProcessor;
        } else {
            return noMillisecondGenerousBeanRowProcessor;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> queryMap(String sql) throws DaoException {
        try {
            log.debug(sql);
            return queryRunner.<List>query(sql, mapListHandler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> queryBean(String sql, Class<T> bean) throws DaoException {
        try {
            log.debug(sql);
            // TODO: 16/3/23 对每一个beanListHandler应该生成结果
            return queryRunner.<List>query(sql, (ResultSetHandler) new BeanListHandler<>(bean, getBeanRowProcessor()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> queryValue(String sql, String column) throws DaoException {
        try {
            log.debug(sql);
            ResultSetHandler columnListHandler = new ColumnListHandler(column);
            return queryRunner.<List>query(sql, columnListHandler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    /**
     * 在创建k,v类型的结果时可用到
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Map<K, V> queryKeyedValue(String sql, String columnKey, String columnValue) throws DaoException {
        try {
            log.debug(sql);
            FreeKeyedHandler freeKeyedHandler = new FreeKeyedHandler(1, columnKey, 2, columnValue);
            return queryRunner.<Map>query(sql, freeKeyedHandler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    /**
     * 在创建k,List<v>类型的结果时可用到
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Map<K, List<V>> queryKeyedValueList(String sql, String columnKey, String columnValue) throws DaoException {
        try {
            log.debug(sql);
            FreeKeyedMultiValueHandler freeKeyedHandler = new FreeKeyedMultiValueHandler(1, columnKey, 2, columnValue);
            return queryRunner.<Map>query(sql, freeKeyedHandler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    /**
     * 在创建k,Object类型的结果时可用到
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K, T> Map<K, T> queryKeyedBean(String sql, Class<T> bean, String key) throws DaoException {
        try {
            log.debug(sql);
            GenerousBeanMapHandler beanMapHandler = new GenerousBeanMapHandler<>(bean, getBeanRowProcessor(), 1, key);
            return queryRunner.<Map>query(sql, beanMapHandler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    /**
     * 在创建k,List<Object>类型的结果时可用到
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K, T> Map<K, List<T>> queryKeyedBeanList(String sql, Class<T> bean, String key) throws DaoException {
        try {
            log.debug(sql);
            GenerousBeanMapListHandler beanMapListHandler = new GenerousBeanMapListHandler<>(bean, getBeanRowProcessor(), 1, key);
            return queryRunner.<Map>query(sql, beanMapListHandler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    /**
     * 在创建k,List<Map>类型的结果时可用到
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K> Map<K, List<Map>> queryKeyedMapList(String sql, String key) throws DaoException {
        try {
            log.debug(sql);
            FreeKeyedMapsListHandler beanMapListHandler = new FreeKeyedMapsListHandler<>(1, key, getBeanRowProcessor());
            return queryRunner.<Map>query(sql, beanMapListHandler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    /**
     * 在创建k,Map类型的结果时可用到
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K> Map<K, Map> queryKeyedMap(String sql, String key) throws DaoException {
        try {
            log.debug(sql);
            FreeKeyedMapsHandler beanMapListHandler = new FreeKeyedMapsHandler<>(1, key, getBeanRowProcessor());
            return queryRunner.<Map>query(sql, beanMapListHandler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }
}
