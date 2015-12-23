package com.tqmall.search.common.result;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by xing on 15/12/4.
 * 分页参数返回
 */
public class PageResult<T> extends Result<Collection<T>> implements Serializable {

    private static final long serialVersionUID = -640667405378706383L;

    private final long total;

    public PageResult(Collection<T> data, long total) {
        super(data);
        this.total = total;
    }

    public PageResult(ErrorCode errorCode) {
        super(errorCode);
        this.total = 0;
    }

    public long getTotal() {
        return total;
    }

}
