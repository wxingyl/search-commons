package com.tqmall.search.db;

import com.tqmall.search.db.param.BeanQueryParam;
import com.tqmall.search.db.param.SqlStatements;
import lombok.Data;
import org.apache.commons.dbutils.handlers.BeanMapHandler;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by xing on 16/4/5.
 *
 * @author xing
 */
public class QueryTest {

    @Test
    public void beanQueryTest() {
        beanQuery(SaintCustomerJoinAudit.class);
        BeanMapHandler<Integer, SaintCustomerJoinAudit> beanMapHandler = Queries.beanMapHandler(SaintCustomerJoinAudit.class, "id");
        int[] a = new int[1024 * 1024 * 8];
        Arrays.fill(a, -1);
        a = new int[1024 * 1024 * 8];
        Arrays.fill(a, -3);
        beanMapHandler = null;
        a = new int[1024 * 1024 * 8];
        Arrays.fill(a, -5);
        System.gc();
        System.out.println("");
    }

    private <T> void beanQuery(Class<T> cls) {
        BeanQueryParam<T> param = Queries.beanParam(SqlStatements.toUnderscoreCase(cls.getSimpleName()), cls);
        param.setQueryCondition(Queries.DEFAULT_DELETED_CONTAINER);
        System.out.println(param.sqlStatement());
    }

    @Data
    static class SaintCustomerJoinAudit {

        private Integer id;

        private Integer customerId;//对应saint_customer_extend.customer_id, 也就是shop_id了

        private String longitude;//纬度

        private String latitude;//经度

        private String majorCarBrand; //专修品牌(这个是拼出来的)

        private Integer showStatus;

        private Integer status;

        private Integer headCount;

        private Integer workingTime;

        private String saName;

        private String saImg;

        private String gmtCreate;

        private String isOpenLegendAccount;

    }

}
