package com.tqmall.search.commons.param.rpc;

import com.tqmall.search.commons.param.PageParam;

/**
 * Created by xing on 16/1/24.
 * RpcParam支持分页
 */
public class RpcPageParam extends RpcParam {

    private static final long serialVersionUID = 6129622988284535969L;

    /**
     * default 10
     */
    private final int size;

    private int start;

    public RpcPageParam () {
        this(10);
    }

    public RpcPageParam (int size) {
        this.size = size;
    }

    public RpcPageParam(PageParam param) {
        super(param);
        start = param.getStart();
        size = param.getSize();
    }

    public RpcPageParam setStart(int start) {
        this.start = start;
        return this;
    }

    public int getSize() {
        return size;
    }

    public int getStart() {
        return start;
    }

}
