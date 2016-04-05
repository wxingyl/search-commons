package com.tqmall.search.db;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

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

    @Override
    public final <T> T query(QueryParam param, ResultSetHandler<T> rsh) {
        String sql = null;
        try {
            return queryRunner.query(sql = param.sqlStatement(), rsh);
        } catch (SQLException e) {
            log.error("run sql query: " + sql + " have exception", e);
            return null;
        }
    }
}
