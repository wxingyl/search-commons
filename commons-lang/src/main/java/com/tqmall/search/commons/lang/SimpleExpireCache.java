package com.tqmall.search.commons.lang;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * date 16/8/16 上午9:41
 * 简单的时间过期cache, 只能用于小对象的cache
 *
 * @author 尚辰
 */
public class SimpleExpireCache<T> {

    /**
     * 初始化之后过期的时间, 单位毫秒ms
     */
    private final long expireAfterInit;

    private final Supplier<T> supplier;

    private T instance;

    private volatile long expireTime;

    public SimpleExpireCache(long expireAfterInitWithMs, Supplier<T> supplier) {
        if (expireAfterInitWithMs <= 0) {
            throw new IllegalArgumentException("expireAfterInitWithMs must gt 0");
        }
        this.expireAfterInit = expireAfterInitWithMs;
        Objects.requireNonNull(supplier);
        this.supplier = supplier;
    }

    public T getInstance() {
        long beforeInitTime = System.currentTimeMillis();
        if (expireTime <= beforeInitTime) {
            synchronized (this) {
                if (expireTime <= beforeInitTime) {
                    instance = supplier.get();
                    expireTime = expireAfterInit + System.currentTimeMillis();
                }
            }
        }
        return instance;
    }

    public void clear() {
        expireTime = 0;
        instance = null;
    }

    public static <T> SimpleExpireCache<T> create(long expireAfterInit, TimeUnit timeUnit, Supplier<T> supplier) {
        return new SimpleExpireCache<T>(timeUnit.toMillis(expireAfterInit), supplier);
    }
}
