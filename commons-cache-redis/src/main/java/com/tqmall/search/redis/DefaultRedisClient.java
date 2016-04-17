package com.tqmall.search.redis;

import com.tqmall.search.commons.utils.StrValueConverts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * date 16/4/16 下午10:29
 * 需要指定Bean的{@link BytesConvert}, 默认为序列化{@link BytesConverts#serializedBeanConvert()}
 * 当然可以以json字符串存储
 *
 * @author 尚辰
 * @see BytesConverts#jsonBeanConvert()
 */
public class DefaultRedisClient<J extends Jedis> extends BaseRedisClient<J> {

    private static final Logger log = LoggerFactory.getLogger(DefaultRedisClient.class);

    private final BytesConvert beanBytesConvert;

    public DefaultRedisClient(Pool<J> jedisPool) {
        this(jedisPool, BytesConverts.serializedBeanConvert());
    }

    public DefaultRedisClient(Pool<J> jedisPool, BytesConvert beanBytesConvert) {
        super(jedisPool);
        this.beanBytesConvert = beanBytesConvert;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> byte[] bytesConvert(T value) {
        return ((BytesConvert<T>) bytesConvert(value.getClass())).toBytes(value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> BytesConvert<T> bytesConvert(Class<T> cls) {
        if (StrValueConverts.getConvert(cls) == null) {
            return beanBytesConvert;
        } else {
            return BytesConverts.basicConvert(cls);
        }
    }

    /**
     * 讲字节数组初始化为对用的值
     *
     * @return 初始化异常或者"nil"则return null
     */
    private <T> T initValue(String key, byte[] values, Class<T> cls) {
        //提前判断"nil", 避免后面初始化抛出异常
        if (Arrays.equals(BytesConverts.NIL_BYTES, values)) return null;
        T val;
        try {
            val = bytesConvert(cls).initBytes(values, cls);
        } catch (Throwable e) {
            log.error("read cache key: " + key + " init value have exception, here return null, it may be have error", e);
            val = null;
        }
        return val;
    }

    @Override
    public <T> boolean set(final String key, final T value) {
        Objects.requireNonNull(value);
        return runTask(new Task<J, Boolean>() {

            @Override
            public Boolean run(J jedis) {
                return StrValueConverts.boolConvert(jedis.set(STR_BC.toBytes(key),
                        bytesConvert(value)));
            }
        });
    }

    @Override
    public <T> boolean set(final String key, final T value, long expireTime, TimeUnit unit) {
        if (expireTime <= 0) {
            return set(key, value);
        }
        Objects.requireNonNull(value);
        final Map.Entry<Long, byte[]> et = expireTimeFormat(expireTime, unit);
        return runTask(new Task<J, Boolean>() {

            @Override
            public Boolean run(J jedis) {
                return StrValueConverts.boolConvert(jedis.set(STR_BC.toBytes(key),
                        bytesConvert(value), BytesConverts.EMPTY_BYTES,
                        et.getValue(), et.getKey()));
            }
        });
    }

    @Override
    public <T> boolean set(final String key, final T value, final boolean isNx) {
        Objects.requireNonNull(value);
        return runTask(new Task<J, Boolean>() {

            @Override
            public Boolean run(J jedis) {
                return StrValueConverts.boolConvert(jedis.set(STR_BC.toBytes(key),
                        bytesConvert(value),
                        isNx ? BytesConverts.NX_BYTES : BytesConverts.XX_BYTES));
            }
        });
    }

    @Override
    public <T> boolean set(final String key, final T value, long expireTime, TimeUnit unit, final boolean isNx) {
        if (expireTime <= 0) {
            return set(key, value, isNx);
        }
        Objects.requireNonNull(value);
        expireTime = unit.toMillis(expireTime);
        final Map.Entry<Long, byte[]> et = expireTimeFormat(expireTime, unit);
        return runTask(new Task<J, Boolean>() {

            @Override
            public Boolean run(J jedis) {
                return StrValueConverts.boolConvert(jedis.set(STR_BC.toBytes(key),
                        bytesConvert(value), isNx ? BytesConverts.NX_BYTES : BytesConverts.XX_BYTES,
                        et.getValue(), et.getKey()));
            }
        });
    }

    @Override
    public <T> T get(final String key, final Class<T> valueCls) {
        return runTask(new Task<J, T>() {

            @Override
            public T run(J jedis) {
                return initValue(key, jedis.get(STR_BC.toBytes(key)), valueCls);
            }
        });
    }

    @Override
    public <T> T getSet(final String key, final T value) {
        return runTask(new Task<J, T>() {

            @Override
            public T run(J jedis) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                Class<T> cls = (Class<T>) value.getClass();
                return initValue(key, jedis.getSet(STR_BC.toBytes(key), bytesConvert(value)), cls);
            }
        });
    }
}
