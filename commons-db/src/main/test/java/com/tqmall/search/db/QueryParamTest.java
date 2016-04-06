package com.tqmall.search.db;

import lombok.Data;
import org.junit.Test;

/**
 * Created by xing on 16/4/5.
 *
 * @author xing
 */
public class QueryParamTest {

    @Test
    public void beanQueryTest() {
        beanQuery(SaintCustomerJoinAudit.class);
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
