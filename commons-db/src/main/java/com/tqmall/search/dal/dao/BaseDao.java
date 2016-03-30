package com.tqmall.search.dal.dao;

import com.tqmall.search.dal.exception.DaoException;
import com.tqmall.search.dal.processor.FreeKeyedHandler;
import com.tqmall.search.dal.processor.FreeKeyedMultiValueHandler;
import com.tqmall.search.dal.processor.GenerousBeanMapHandler;
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

    private QueryRunner queryRunner;
    private static final ResultSetHandler mapListHandler = new MapListHandler();
    private static final RowProcessor generousBeanRowProcessor = new BasicRowProcessor(new GenerousBeanProcessor());

    public BaseDao(DataSource dataSource) {
        queryRunner = new QueryRunner(dataSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> query(String sql) throws DaoException {
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
    public <T> List<T> query(String sql, Class<T> bean) throws DaoException {
        try {
            log.debug(sql);
            // TODO: 16/3/23 对每一个beanListHandler应该生成缓存
            return queryRunner.<List>query(sql, (ResultSetHandler) new BeanListHandler<>(bean, generousBeanRowProcessor));
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> query(String sql, String column) throws DaoException {
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
     * 在创建k,v类型的缓存时可用到
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Map<K, V> query(String sql, String columnKey, String columnValue) throws DaoException {
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
     * 在创建k,List<v>类型的缓存时可用到
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Map<K, List<V>> query(String sql, String columnKey, String columnValue, Void ignore) throws DaoException {
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
     * 在创建k,Object类型的缓存时可用到
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K, T> Map<K, T> query(String sql, Class<T> bean, String key) throws DaoException {
        try {
            log.debug(sql);
            GenerousBeanMapHandler beanMapHandler = new GenerousBeanMapHandler<>(bean, generousBeanRowProcessor, 1, key);
            return queryRunner.<Map>query(sql, beanMapHandler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }
}
