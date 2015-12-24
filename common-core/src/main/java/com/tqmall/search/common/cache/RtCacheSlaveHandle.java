package com.tqmall.search.common.cache;

import java.util.List;

/**
 * Created by xing on 15/12/22.
 * slave机器缓存实时更新
 */
public interface RtCacheSlaveHandle {

    /**
     * 该缓存是否已经初始化
     * 只有当该缓存初始化了,调用{@link #onSlaveHandle(List)} 才有意义, 还没有初始化可以自己去初始化,无需动态修改
     */
    boolean initialized();

    /**
     * 参数统一用String提供,自己内部做转换
     * 处理Slave缓存时,如果还没有初始化, 则没有必要更新
     * @param keys 变动的key list, 参数必须
     * @return 看是否有改动
     */
    boolean onSlaveHandle(List<String> keys);

}
