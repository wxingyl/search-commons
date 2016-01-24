package com.tqmall.search.commons.param;

import com.tqmall.search.commons.utils.SearchStringUtils;

/**
 * Created by xing on 15/12/5.
 * 具有关键字查询的参数, 所有的查询接口, 都支持分页的
 */
public class KeywordParam extends PageParam {

    private static final long serialVersionUID = 1L;
    /**
     * 关键字为trim之后的值
     */
    private String q;

    public void setQ(String q) {
        this.q = SearchStringUtils.filterString(q);
    }

    public String getQ() {
        return q;
    }
}
