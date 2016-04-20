package com.tqmall.search.db;

import com.tqmall.search.commons.condition.Conditions;
import com.tqmall.search.db.param.BeanQueryParam;
import com.tqmall.search.db.param.SqlStatements;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.handlers.BeanMapHandler;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

/**
 * Created by xing on 16/4/5.
 *
 * @author xing
 */
public class QueryTest {

    private static MysqlQueryRunner queryRunner;

    @BeforeClass
    public static void init() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:mysql://127.0.0.1:3306?characterEncoding=UTF-8");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        queryRunner = new DefaultQueryRunner(dataSource);
    }

    @Test
    public void sqlStatementTest() {
        StringBuilder result = new StringBuilder(256);
        SqlStatements.appendContainer(result, Queries.DEFAULT_DELETED_CONTAINER);
        String expected = "`is_deleted` = 'N'";
        Assert.assertEquals(expected, result.toString());
        result.setLength(0);

        SqlStatements.appendContainer(result, Conditions.unmodifiableContainer()
                .mustCondition(Conditions.equal("status", 1))
                .mustCondition(Queries.DEFAULT_DELETED_CONTAINER)
                .shouldCondition(Conditions.in("id", Integer.TYPE, 1, 2, 3))
                .shouldCondition(Conditions.in("name", String.class, "xing", "tqmall"))
                .create());
        expected = "`status` = 1 AND (`is_deleted` = 'N') AND (`id` IN (1, 2, 3) OR `name` IN ('xing', 'tqmall'))";
        Assert.assertEquals(expected, result.toString());
        result.setLength(0);

        SqlStatements.appendContainer(result, Conditions.unmodifiableContainer()
                .mustCondition(Conditions.equal("status", 1))
                .mustCondition(Conditions.unmodifiableContainer()
                        .shouldCondition(Conditions.equal("is_deleted", "N"))
                        .shouldCondition(Conditions.equal("flag", 3))
                        .create())
                .shouldCondition(Conditions.in("id", Integer.TYPE, 1, 2, 3))
                .shouldCondition(Conditions.in("name", String.class, "xing", "tqmall"))
                .create());
        expected = "`status` = 1 AND (`is_deleted` = 'N' OR `flag` = 3) AND (`id` IN (1, 2, 3) OR `name` IN ('xing', 'tqmall'))";
        Assert.assertEquals(expected, result.toString());
        result.setLength(0);
    }

    @Test
    @Ignore
    public void beanQueryTest() {
        BeanQueryParam<Country> beanQueryParam = Queries.beanParam("tqdb_base", "db_country", Country.class);
        beanQueryParam.setQueryCondition(Queries.DEFAULT_DELETED_CONTAINER);
        BeanMapHandler<Integer, Country> beanMapHandler = Queries.beanMapHandler(Country.class, "id");
        Map<Integer, Country> dataMap = queryRunner.query(beanQueryParam, beanMapHandler);
        Assert.assertFalse(dataMap.isEmpty());
        System.out.println(dataMap);
    }

    /**
     * CREATE TABLE `db_country` (
     * `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
     * `gmt_create` datetime DEFAULT NULL,
     * `gmt_modified` datetime DEFAULT NULL,
     * `creator` int(11) DEFAULT NULL,
     * `modifier` int(11) DEFAULT NULL,
     * `is_deleted` varchar(1) DEFAULT 'N',
     * `country_code` int(11) NOT NULL DEFAULT '0' COMMENT '国家编码',
     * `country_name` varchar(20) NOT NULL DEFAULT '' COMMENT '国家名称',
     * `short_code` varchar(20) NOT NULL DEFAULT '' COMMENT '英文简写',
     * PRIMARY KEY (`id`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='国家字典表';
     */
    public static class Country {

        private Integer id;

        private Integer countryCode;

        private String countryName;

        private String shortCode;

        public Integer getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(Integer countryCode) {
            this.countryCode = countryCode;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getShortCode() {
            return shortCode;
        }

        public void setShortCode(String shortCode) {
            this.shortCode = shortCode;
        }

        @Override
        public String toString() {
            return "Country{" +
                    "countryCode=" + countryCode +
                    ", id=" + id +
                    ", countryName='" + countryName + '\'' +
                    ", shortCode='" + shortCode + '\'' +
                    '}';
        }
    }

}
