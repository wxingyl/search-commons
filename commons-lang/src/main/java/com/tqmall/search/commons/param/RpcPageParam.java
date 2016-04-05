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

    public final void setStart(int start) {
        this.start = start;
    }

    /**
     * 通过设定页码pageNo的方式设定start, pageNo从1开始计算
     *
     * @param pageNo 页码, 从1开始计算
     */
    public final void setPageNo(int pageNo) {
        this.start = (pageNo - 1) * size;
    }

    public final int getSize() {
        return size;
    }

    public final int getStart() {
        return start;
    }

}
