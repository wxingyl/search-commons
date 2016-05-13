package com.tqmall.search.commons.rcache;

import com.tqmall.search.commons.lang.Cache;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.SearchStringUtils;
import com.tqmall.search.redis.RedisClient;

import java.util.*;

/**
 * Created by xing on 16/4/7.
 * redis cache 抽象定义, 其整个作为redis中的map, {@link #mapKey}为该map的key, 默认是该class的{@link Class#getName()}, field通过
 * {@link #mapField(Object)}获取, 默认直接toString()
 *
 * @author xing
 */
public abstract class AbstractRedisCache<K, V> implements Cache<K, V> {

    protected final RedisClient client;

    protected final String mapKey;

    protected final Class<V> valueCls;

    protected AbstractRedisCache(RedisClient client, Class<V> valueCls) {
        this(client, valueCls, null);
    }

    protected AbstractRedisCache(RedisClient client, Class<V> valueCls, String mapKey) {
        this.client = client;
        this.valueCls = valueCls;
        this.mapKey = SearchStringUtils.isEmpty(mapKey) ? this.getClass().getName() : mapKey;
    }

    /**
     * 获取key的String field, 默认直接用{@link #toString()}, 需要修改直接override该方法
     */
    protected String mapField(K key) {
        return key.toString();
    }

    /**
     * 默认实现调用{@link #loadValue(Set)}搞定
     * 自己可以override
     */
    protected V loadValue(K key) {
        String fieldKey = mapField(key);
        Map<String, V> map = loadValue(Collections.singleton(fieldKey));
        return map == null ? null : map.get(fieldKey);
    }

    /**
     * 通过key的String值列表加载值, key的String值通过{@link #mapField(Object)}拿到
     *
     * @param keys key的字符串值set, 调用该函数保证keys有值, 可以不用校验 != null && !{@link Set#isEmpty()}
     */
    protected abstract Map<String, V> loadValue(Set<String> keys);

    @Override
    public V getValue(K key) {
        if (key == null) return null;
        V v = client.hGet(mapKey, mapField(key), valueCls);
        if (v == null) {
            v = loadValue(key);
            if (v != null) {
                client.hSet(mapKey, mapField(key), v);
            }
        }
        return v;
    }

    @Override
    public Map<K, V> getValue(Iterable<K> keys) {
        Map<K, V> retMap = new HashMap<>();
        for (K key : keys) {
            V v = getValue(key);
            if (v != null) {
                retMap.put(key, v);
            }
        }
        return retMap;
    }

    /**
     * redis本身就是原子操作, 所以这儿不用管多线程修改的问题
     * 另外缓存本身多删除或者少删除没有什么影响
     */
    @Override
    public int reload() {
        Set<String> keys = client.hKeys(mapKey);
        if (CommonsUtils.isEmpty(keys)) return 0;
        Map<String, V> values = loadValue(keys);
        List<String> needRemoveKeys = new ArrayList<>();
        for (String k : keys) {
            V v = values.get(k);
            if (v == null) {
                needRemoveKeys.add(k);
            } else {
                client.hSet(mapKey, k, v);
            }
        }
        //没有用的field删除
        if (needRemoveKeys.isEmpty()) {
            return keys.size();
        } else {
            int length = needRemoveKeys.size();
            client.hDel(mapKey, needRemoveKeys.toArray(new String[length]));
            return keys.size() - length;
        }
    }

    @Override
    public void clear() {
        client.del(mapKey);
    }
}
