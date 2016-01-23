package com.tqmall.search.commons.param;

/**
 * Created by xing on 15/12/5.
 * 支持分页的参数类
 */
public abstract class PageParam extends Param {

    private static final long serialVersionUID = 1L;

    private int start;

    /**
     * default 10
     */
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

    /**
     * 同{@link #setSize(int)}
     *
     * @see #setSize(int)
     */
    public void setLimit(int limit) {
        setSize(limit);
    }

    /**
     * 同{@link #setSize(int)}
     *
     * @see #setSize(int)
     */
    public void setPageSize(int pageSize) {
        setSize(pageSize);
    }

    public void setStart(int start) {
        if (start < 0) return;
        this.start = start;
    }

    /**
     * 同{@link #setStart(int)}
     *
     * @see #setStart(int)
     */
    public void setFrom(int from) {
        setStart(from);
    }

}
