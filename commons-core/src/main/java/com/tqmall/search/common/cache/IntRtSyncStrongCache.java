package com.tqmall.search.common.cache;


import com.tqmall.search.common.utils.HostInfo;
import com.tqmall.search.common.utils.StrValueConvert;
import com.tqmall.search.common.utils.StrValueConverts;

import java.util.List;
import java.util.Map;

/**
 * Created by xing on 15/12/10.
 * key 为 Integer类型的实时索引缓存, 并且一些抽象方法做了默认实现
 */
public abstract class IntRtSyncStrongCache<T> extends AbstractRtSyncStrongCache<Integer, T> {

    public IntRtSyncStrongCache(HostInfo masterHost) {
        super(masterHost);
    }

    @Override
    protected final StrValueConvert<Integer> getKeyValueConvert() {
        return StrValueConverts.getConvert(Integer.TYPE);
    }

    /**
     * 默认该方法与{@link #reloadValue(List)}等同,只不过传的参数不一样而已
     */
    @Override
    protected Map<Integer, T> loadCache() {
        return reloadValue(null);
    }

    /**
     * 这儿默认直接调用 {@link #updateValue(Object, Object)}
     * @return 是否更新
     */
    @Override
    protected boolean onSlaveHandle(Integer key, T value) {
        return updateValue(key, value);
    }
}
