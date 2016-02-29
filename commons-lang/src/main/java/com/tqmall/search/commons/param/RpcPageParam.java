package com.tqmall.search.commons.param;

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

    public RpcPageParam() {
        this(10);
    }

    public RpcPageParam(int size) {
        this.size = size;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getSize() {
        return size;
    }

    public int getStart() {
        return start;
    }

}
