package com.tqmall.search.common.cache;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xing on 15/12/1.
 * 强引用缓存抽象类,所有的缓存建议都继承该类去实现
 * 所有缓存按需加载
 * 通过Map做基本的缓存, 为强引用类型, GC无法回收的, 所以只缓存数据比较小的对象
 * 日后可以考虑弱类型缓存, 通过guava的{@link LoadingCache}实现
 */
public abstract class AbstractStrongCache<K, V> implements StrongCache<K, V> {

    private ConcurrentMap<K, V> cache;

    protected abstract Map<K, V> loadCache();

    /**
     * call this method, you should make sure has initialized cache
     * @return 更新操作是否有更改
     *      如果是删除操作,看原先是否为null,如果就是null活着不存在,则认为没有更改,返回false
     *      如果是更新操作, 看原先两者是不是{@link Objects#equals(Object, Object)}
     */
    final protected boolean updateValue(K key, V val) {
        V before;
        if (val == null) {
            before = cache.remove(key);
        } else {
            before = cache.put(key, val);
        }
        return !Objects.equals(val, before);
    }

    private synchronized int init() {
        if (cache == null) {
            Map<K, V> data = loadCache();
            if (data == null || data.isEmpty()) {
                cache = new ConcurrentHashMap<>();
            } else if (data instanceof ConcurrentMap){
                cache = (ConcurrentMap<K, V>) data;
            } else {
                cache = Maps.newConcurrentMap();
                cache.putAll(data);
            }
        }
        return cache.size();
    }

    @Override
    public V getValue(K key) {
        if(key == null) return null;
        if (cache == null) {
            init();
        }
        return cache.get(key);
    }

    /**
     * 返回值保证不为null
     */
    @Override
    public final Map<K, V> getAllCache() {
        if (cache == null) {
            init();
        }
        return Collections.unmodifiableMap(cache);
    }

    @Override
    final public boolean initialized() {
        return cache != null;
    }

    /**
     * 提供reload 函数
     */
    @Override
    final public synchronized int reload() {
        clear();
        return init();
    }

    @Override
    public synchronized void clear() {
        cache = null;
    }

}
