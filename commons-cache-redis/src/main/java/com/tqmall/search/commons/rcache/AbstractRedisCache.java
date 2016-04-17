package com.tqmall.search.commons.rcache;

import com.tqmall.search.commons.lang.Cache;
import com.tqmall.search.redis.RedisClient;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

/**
 * Created by xing on 16/4/7.
 * redis cache 抽象定义
 *
 * @author xing
 */
public abstract class AbstractRedisCache<K, V> implements Cache<K, V> {

    private final RedisClient client;

    protected AbstractRedisCache(RedisClient client) {
        this.client = client;
    }

}
