package com.tqmall.search.common.param;

/**
 * Created by xing on 15/12/5.
 * 支持分页的参数类
 */
public abstract class PageParam extends Param {

    private static final long serialVersionUID = 1L;

    private int start;

    private int size = 10;

    public int getSize() {
        return size;
    }

    public int getStart() {
        return start;
    }

    public void setSize(int size) {
        if (size < 0) return;
        this.size = size;
    }

    public void setStart(int start) {
        if (start < 0) return;
        this.start = start;
    }

    public void setFrom(int from) {
        setStart(from);
    }
}
