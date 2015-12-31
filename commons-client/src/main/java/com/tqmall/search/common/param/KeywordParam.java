package com.tqmall.search.common.param;

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
        this.q = filterString(q);
    }

    public String getQ() {
        return q;
    }
}
