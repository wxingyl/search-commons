package com.tqmall.search.commons.param;

/**
 * Created by xing on 16/2/29.
 * 具有关键字查询的Rpc参数, 所有的查询接口, 都支持分页的
 */
public class RpcKeywordParam extends RpcPageParam {

    private static final long serialVersionUID = -199966874420900080L;

    private final String q;

    public RpcKeywordParam(String q) {
        super();
        this.q = q;
    }

    public RpcKeywordParam(String q, int size) {
        super(size);
        this.q = q;
    }

    public String getQ() {
        return q;
    }
}
