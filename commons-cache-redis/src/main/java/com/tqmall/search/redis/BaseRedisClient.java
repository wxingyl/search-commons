package com.tqmall.search.redis;

import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.StrValueConverts;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * date 16/4/16 下午10:35
 * 实现一些基本的redis命令
 */
public abstract class BaseRedisClient<J extends Jedis> extends JedisTask<J> implements RedisClient {

    protected BaseRedisClient(Pool<J> jedisPool) {
        super(jedisPool);
    }

    @Override
    public boolean exist(final String key) {
        return runTask(new Task<J, Boolean>() {
            @Override
            public Boolean run(J jedis) {
                return jedis.exists(STR_BC.toBytes(key));
            }
        });
    }

    @Override
    public boolean del(final String key) {
        return runTask(new Task<J, Boolean>() {
            @Override
            public Boolean run(J jedis) {
                Long num = jedis.del(STR_BC.toBytes(key));
                return num != null && num != 0;
            }
        });
    }

    @Override
    public long del(final String... keys) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.del(getBytes(keys));
            }
        });
    }

    @Override
    public long decr(final String key) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.decr(STR_BC.toBytes(key));
            }
        });
    }

    @Override
    public long decrBy(final String key, final long step) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.decrBy(STR_BC.toBytes(key), step);
            }
        });
    }

    @Override
    public long incr(final String key) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.incr(STR_BC.toBytes(key));
            }
        });
    }

    @Override
    public long incrBy(final String key, final long step) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.incrBy(STR_BC.toBytes(key), step);
            }
        });
    }

    @Override
    public double incrByFloat(final String key, final double step) {
        return runTask(new Task<J, Double>() {
            @Override
            public Double run(J jedis) {
                return jedis.incrByFloat(STR_BC.toBytes(key), step);
            }
        });
    }

    @Override
    public boolean expire(final String key, final long expireTime, final TimeUnit unit) {
        return runTask(new Task<J, Boolean>() {
            @Override
            public Boolean run(J jedis) {
                long time = unit.toMillis(expireTime);
                Long ret;
                if (time % 1000 == 0) {
                    ret = jedis.expire(STR_BC.toBytes(key), (int) (time / 1000));
                } else {
                    ret = jedis.pexpire(STR_BC.toBytes(key), time);
                }
                return ret != null && 1 == ret;
            }
        });
    }

    @Override
    public long ttl(final String key) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.ttl(STR_BC.toBytes(key));
            }
        });
    }

    @Override
    public long pttl(final String key) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.pttl(STR_BC.toBytes(key));
            }
        });
    }

    @Override
    public boolean persist(final String key) {
        return runTask(new Task<J, Boolean>() {
            @Override
            public Boolean run(J jedis) {
                Long ret = jedis.persist(STR_BC.toBytes(key));
                return ret != null && ret == 1;
            }
        });
    }

    @Override
    public String type(final String key) {
        return runTask(new Task<J, String>() {
            @Override
            public String run(J jedis) {
                return jedis.type(STR_BC.toBytes(key));
            }
        });
    }

    @Override
    public boolean setBit(final String key, final long offset, final boolean value) {
        return runTask(new Task<J, Boolean>() {
            @Override
            public Boolean run(J jedis) {
                return jedis.setbit(STR_BC.toBytes(key), offset, value);
            }
        });
    }

    @Override
    public boolean getBit(final String key, final long offset) {
        return runTask(new Task<J, Boolean>() {
            @Override
            public Boolean run(J jedis) {
                return jedis.getbit(STR_BC.toBytes(key), offset);
            }
        });
    }

    @Override
    public long bitCount(final String key) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.bitcount(STR_BC.toBytes(key));
            }
        });
    }

    @Override
    public long bitCount(final String key, final long byteStart, final long byteEnd) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.bitcount(STR_BC.toBytes(key), byteStart, byteEnd);
            }
        });
    }

    @Override
    public long bitPos(final String key, final boolean value) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                Long ret = jedis.bitpos(STR_BC.toBytes(key), value);
                if (ret == null || ret < 0) return -1L;
                else return ret;
            }
        });
    }

    @Override
    public long bitPos(final String key, final boolean value, final long byteStart, final long byteEnd) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                BitPosParams params = byteEnd < 0 ? new BitPosParams(byteStart) : new BitPosParams(byteStart, byteEnd);
                Long ret = jedis.bitpos(key, value, params);
                if (ret == null || ret < 0) return -1L;
                else return ret;
            }
        });
    }

    @Override
    public long setRange(final String key, final long offset, final String value) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.setrange(STR_BC.toBytes(key), offset, STR_BC.toBytes(value));
            }
        });
    }

    @Override
    public String getRange(final String key, final long startOffset, final long endOffset) {
        return runTask(new Task<J, String>() {
            @Override
            public String run(J jedis) {
                return STR_BC.initBytes(jedis.getrange(STR_BC.toBytes(key), startOffset, endOffset), String.class);
            }
        });
    }

    @Override
    public long hIncrBy(final String key, final String field, final long value) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.hincrBy(STR_BC.toBytes(key), STR_BC.toBytes(field), value);
            }
        });
    }

    @Override
    public double hIncrByFloat(final String key, final String field, final double value) {
        return runTask(new Task<J, Double>() {
            @Override
            public Double run(J jedis) {
                return jedis.hincrByFloat(STR_BC.toBytes(key), STR_BC.toBytes(field), value);
            }
        });
    }

    @Override
    public boolean hExist(final String key, final String field) {
        return runTask(new Task<J, Boolean>() {
            @Override
            public Boolean run(J jedis) {
                return jedis.hexists(STR_BC.toBytes(key), STR_BC.toBytes(field));
            }
        });
    }

    @Override
    public boolean hDel(final String key, final String... fields) {
        if (fields.length == 0) return false;
        return runTask(new Task<J, Boolean>() {
            @Override
            public Boolean run(J jedis) {
                Long ret = jedis.hdel(STR_BC.toBytes(key), getBytes(fields));
                return ret != null && ret == 1;
            }
        });
    }

    @Override
    public long hLen(final String key) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                return jedis.hlen(STR_BC.toBytes(key));
            }
        });
    }

    @Override
    public Set<String> hKeys(final String key) {
        return runTask(new Task<J, Set<String>>() {
            @Override
            public Set<String> run(J jedis) {
                Set<byte[]> sets = jedis.hkeys(STR_BC.toBytes(key));
                if (CommonsUtils.isEmpty(sets)) {
                    return Collections.emptySet();
                } else {
                    Set<String> strSet = new HashSet<>(sets.size());
                    for (byte[] bs : sets) {
                        strSet.add(new String(bs, StandardCharsets.UTF_8));
                    }
                    return strSet;
                }
            }
        });
    }

    @Override
    public long lLen(final String key) {
        return runTask(new Task<J, Long>() {
            @Override
            public Long run(J jedis) {
                Long ret = jedis.llen(STR_BC.toBytes(key));
                return ret == null ? 0 : ret;
            }
        });
    }

    @Override
    public boolean lTrim(final String key, final long start, final long end) {
        return runTask(new Task<J, Boolean>() {
            @Override
            public Boolean run(J jedis) {
                return StrValueConverts.boolConvert(jedis.ltrim(STR_BC.toBytes(key), start, end));
            }
        });
    }
}
