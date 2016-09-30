package com.tqmall.search.redis;

import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.StrValueConverts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    private <T> List<T> convertToList(String key, Class<T> valueCls, List<byte[]> bys) {
        if (CommonsUtils.isEmpty(bys)) return Collections.emptyList();
        else {
            List<T> list = new ArrayList<>(bys.size());
            for (byte[] bs : bys) {
                list.add(initValue(key, bs, valueCls));
            }
            return list;
        }
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

    @Override
    public <T> boolean hSet(final String key, final String field, final T value) {
        Objects.requireNonNull(value);
        return runTask(new Task<J, Boolean>() {
            @Override
            public Boolean run(J jedis) {
                Long ret = jedis.hset(STR_BC.toBytes(key), STR_BC.toBytes(field), bytesConvert(value));
                return ret != null && ret != 0;
            }
        });
    }

    @Override
    public <T> T hGet(final String key, final String field, final Class<T> valueCls) {
        return runTask(new Task<J, T>() {
            @Override
            public T run(J jedis) {
                return initValue(key, jedis.hget(STR_BC.toBytes(key),
                        STR_BC.toBytes(field)), valueCls);
            }
        });
    }

    @Override
    public <T> boolean hSetNx(final String key, final String field, final T value) {
        Objects.requireNonNull(value);
        return runTask(new Task<J, Boolean>() {
            @Override
            public Boolean run(J jedis) {
                Long ret = jedis.hset(STR_BC.toBytes(key), STR_BC.toBytes(field), bytesConvert(value));
                return ret != null && ret != 0;
            }
        });
    }

    @Override
    public <T> boolean hmSet(final String key, final Map<String, T> valueMap) {
        if (CommonsUtils.isEmpty(valueMap)) return false;
        return runTask(new Task<J, Boolean>() {
            @Override
            public Boolean run(J jedis) {
                Map<byte[], byte[]> valueBys = new HashMap<>(valueMap.size());
                for (Map.Entry<String, T> e : valueMap.entrySet()) {
                    valueBys.put(STR_BC.toBytes(e.getKey()), bytesConvert(e.getValue()));
                }
                jedis.hmset(STR_BC.toBytes(key), valueBys);
                return null;
            }
        });
    }

    @Override
    public <T> List<T> hmGet(final String key, final Class<T> valueCls, final String... fields) {
        if (fields.length == 0) return Collections.emptyList();
        return runTask(new Task<J, List<T>>() {
            @Override
            public List<T> run(J jedis) {
                List<byte[]> bys = jedis.hmget(STR_BC.toBytes(key), getBytes(fields));
                return convertToList(key, valueCls, bys);
            }
        });
    }

    @Override
    public <T> List<T> hVals(final String key, final Class<T> valueCls) {
        return runTask(new Task<J, List<T>>() {
            @Override
            public List<T> run(J jedis) {
                List<byte[]> bys = jedis.hvals(STR_BC.toBytes(key));
                return convertToList(key, valueCls, bys);
            }
        });
    }

    @Override
    public <T> Map<String, T> hGetAll(final String key, final Class<T> valueCls) {
        return runTask(new Task<J, Map<String, T>>() {
            @Override
            public Map<String, T> run(J jedis) {
                Map<byte[], byte[]> mapBys = jedis.hgetAll(STR_BC.toBytes(key));
                if (CommonsUtils.isEmpty(mapBys)) return Collections.emptyMap();
                else {
                    Map<String, T> map = new HashMap<>(mapBys.size());
                    for (Map.Entry<byte[], byte[]> e : mapBys.entrySet()) {
                        map.put(new String(e.getKey(), StandardCharsets.UTF_8),
                                initValue(key, e.getValue(), valueCls));
                    }
                    return map;
                }
            }
        });
    }

    @Override
    public <T> long rPush(final String key, final T value) {
        Objects.requireNonNull(value);
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.rpush(STR_BC.toBytes(key), bytesConvert(value));
            }
        });
    }

    @Override
    public <T> long rPush(final String key, final List<T> values) {
        if (CommonsUtils.isEmpty(values)) return -1;
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                byte[][] valBytes = new byte[values.size()][];
                int i = 0;
                for (T v : values) {
                    valBytes[i++] = bytesConvert(v);
                }
                return jedis.rpush(STR_BC.toBytes(key), valBytes);
            }
        });
    }

    @Override
    public <T> long lPush(final String key, final T value) {
        Objects.requireNonNull(value);
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.lpush(STR_BC.toBytes(key), bytesConvert(value));
            }
        });
    }

    @Override
    public <T> long lPush(final String key, final List<T> values) {
        if (CommonsUtils.isEmpty(values)) return -1;
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                byte[][] valBytes = new byte[values.size()][];
                int i = 0;
                for (T v : values) {
                    valBytes[i++] = bytesConvert(v);
                }
                return jedis.lpush(STR_BC.toBytes(key), valBytes);
            }
        });
    }

    @Override
    public <T> List<T> lRange(final String key, final Class<T> cls, final long start, final long end) {
        return runTask(new Task<J, List<T>>() {
            @Override
            public List<T> run(J jedis) {
                List<byte[]> bytesList = jedis.lrange(STR_BC.toBytes(key), start, end);
                return convertToList(key, cls, bytesList);
            }
        });
    }

    @Override
    public <T> T lIndex(final String key, final Class<T> cls, final long index) {
        return runTask(new Task<J, T>() {
            @Override
            public T run(J jedis) {
                return initValue(key, jedis.lindex(STR_BC.toBytes(key), index), cls);
            }
        });
    }

    @Override
    public <T> boolean lSet(final String key, final long index, final T value) {
        Objects.requireNonNull(value);
        return runTask(new Task<J, Boolean>() {
            @Override
            public Boolean run(J jedis) {
                return StrValueConverts.boolConvert(jedis.lset(STR_BC.toBytes(key), index, bytesConvert(value)));
            }
        });
    }

    @Override
    public <T> long lRem(final String key, final long count, final T value) {
        Objects.requireNonNull(value);
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                Long ret = jedis.lrem(STR_BC.toBytes(key), count, bytesConvert(value));
                return ret == null || ret < 0 ? -1 : ret;
            }
        });
    }

    @Override
    public <T> T lPop(final String key, final Class<T> valueCls) {
        return runTask(new Task<J, T>() {
            @Override
            public T run(J jedis) {
                return initValue(key, jedis.lpop(STR_BC.toBytes(key)), valueCls);
            }
        });
    }

    @Override
    public <T> T rPop(final String key, final Class<T> valueCls) {
        return runTask(new Task<J, T>() {
            @Override
            public T run(J jedis) {
                return initValue(key, jedis.rpop(STR_BC.toBytes(key)), valueCls);
            }
        });
    }

}
