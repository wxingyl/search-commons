package com.tqmall.search.redis;

import com.tqmall.search.commons.utils.CommonsUtils;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * date 16/4/16 下午10:35
 * Jedis实例获取必须从{@link Pool}获取
 *
 * @author 尚辰
 * @see redis.clients.jedis.JedisPool
 * @see redis.clients.jedis.JedisSentinelPool
 * @see redis.clients.jedis.ShardedJedisPool
 */
public class JedisTask<J extends Jedis> implements Closeable {

    private final Pool<J> jedisPool;

    /**
     * 字符串的{@link BytesConvert}, 很多key都需要的, 所以砸门就直接放到这
     */
    public final static BytesConvert<String> STR_BC = BytesConverts.basicConvert(String.class);

    public JedisTask(Pool<J> jedisPool) {
        this.jedisPool = jedisPool;
    }

    public final <R> R runTask(Task<J, R> task) {
        try (J jedis = jedisPool.getResource()) {
            return task.run(jedis);
        }
    }

    @Override
    public void close() {
        jedisPool.close();
    }

    //下面都是一些static 方法

    //task定义
    public interface Task<J extends Jedis, R> {
        R run(J jedis);
    }

    public static byte[][] getBytes(String... keys) {
        byte[][] keysBytes = new byte[keys.length][];
        for (int i = 0; i < keys.length; i++) {
            keysBytes[i] = STR_BC.toBytes(keys[i]);
        }
        return keysBytes;
    }

    public static Map.Entry<Long, byte[]> expireTimeFormat(long expireTime, TimeUnit unit) {
        expireTime = unit.toMillis(expireTime);
        if (expireTime % 1000 == 0) {
            return CommonsUtils.newImmutableMapEntry(expireTime / 1000, BytesConverts.EX_BYTES);
        } else {
            return CommonsUtils.newImmutableMapEntry(expireTime, BytesConverts.PX_BYTES);
        }
    }
}
