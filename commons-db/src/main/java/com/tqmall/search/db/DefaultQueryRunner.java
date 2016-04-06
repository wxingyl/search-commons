package com.tqmall.search.db;

import com.tqmall.search.db.param.BeanQueryParam;
import com.tqmall.search.db.param.QueryParam;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.BeanMapHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/4/5.
 *
 * @author xing
 */
public class DefaultQueryRunner implements MysqlQueryRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultQueryRunner.class);

    private final QueryRunner queryRunner;

    public DefaultQueryRunner(DataSource ds) {
        this.queryRunner = new QueryRunner(ds);
    }

    private <T> T query(String sql, ResultSetHandler<T> rsh) {
        try {
            return queryRunner.query(sql, rsh);
        } catch (SQLException e) {
            log.error("run sql query: " + sql + " have exception", e);
            return null;
        }
    }

    @Override
    public <T> T query(QueryParam param, ResultSetHandler<T> rsh) {
        return query(param.sqlStatement(), rsh);
    }

    @Override
    public <T> T query(BeanQueryParam<T> param, BeanHandler<T> handler) {
        return query(param.sqlStatement(), handler);
    }

    @Override
    public <T> List<T> query(BeanQueryParam<T> param, BeanListHandler<T> handler) {
        return query(param.sqlStatement(), handler);
    }

    @Override
    public <K, V> Map<K, V> query(BeanQueryParam<V> param, BeanMapHandler<K, V> handler) {
        return query(param.sqlStatement(), handler);
    }
}
