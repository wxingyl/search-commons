package com.tqmall.search.common.cache;


import com.tqmall.search.common.utils.StrValueConvert;
import com.tqmall.search.common.utils.StrValueConverts;

/**
 * Created by xing on 15/12/10.
 * key 为 Integer类型的实时索引缓存
 */
public abstract class IntRtStrongCache<T> extends AbstractRtStrongCache<Integer, T> {

    public IntRtStrongCache() {
        super();
    }

    @Override
    protected final StrValueConvert<Integer> getKeyValueConvert() {
        return StrValueConverts.getConvert(Integer.TYPE);
    }

}
