package com.tqmall.search.common.cache;

import java.util.List;

/**
 * Created by xing on 15/12/22.
 * slave机器缓存实时更新
 */
public interface RtCacheSlaveHandle {

    /**
     * 参数统一用String提供,自己内部做转换
     * @param keys 变动的key list
     */
    void onSlaveHandle(List<String> keys);

}
