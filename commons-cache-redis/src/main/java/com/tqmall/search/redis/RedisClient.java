package com.tqmall.search.redis;

import java.util.concurrent.TimeUnit;

/**
 * date 16/4/16 下午9:40
 * redis client操作接口定义
 * 类似{@link redis.clients.jedis.JedisCommands}, 只不过涉及到值相关的通过泛型T 来搞定
 * redis命令文档地址: http://redis.io/commands
 * 有个中文版: http://www.redis.cn/commands.html
 * 这儿没有实现所有的redis命令, 只是一些常用的, 其他如果需要, 通过{@link JedisTask}自己去实现吧
 *
 * @author 尚辰
 */
public interface RedisClient {

    /**
     * @param value not null
     * @return 执行是否成功
     */
    <T> boolean set(String key, T value);

    /**
     * 设置能够超时的cache
     *
     * @param value      not null
     * @param expireTime 如果 <= 0, 则同{@link #set(String, Object)}
     * @return 执行是否成功
     */
    <T> boolean set(String key, T value, long expireTime, TimeUnit unit);

    /**
     * 设置cache, 是否为NX, 如果不是NX, 则为XX
     * NX – 只有键key不存在的时候才会设置key的值
     * XX – 只有键key存在的时候才会设置key的值
     *
     * @param value not null
     * @param isNx  是否为NX, 如果不是NX, 则为XX
     * @return 执行是否成功
     */
    <T> boolean set(String key, T value, boolean isNx);

    /**
     * 设置cache, 是否为NX, 并且制定超时
     *
     * @param value      not null
     * @param expireTime 如果 <= 0, 则同{@link #set(String, Object)}
     * @param isNx       是否为NX, 如果不是NX, 则为XX
     * @return 执行是否成功
     */
    <T> boolean set(String key, T value, long expireTime, TimeUnit unit, boolean isNx);

    <T> T get(String key, Class<T> valueCls);

    /**
     * 先get, 再set
     */
    <T> T getSet(String key, T value);

    /**
     * @return 指定的key是否存在
     */
    boolean exist(String key);

    /**
     * @return 是否删除成功
     */
    boolean del(String key);

    /**
     * @return 批量删除成功的key个数
     */
    long del(String... keys);

    long decr(String key);

    long decrBy(String key, long step);

    long incr(String key);

    long incrBy(String key, long step);

    double incrByFloat(String key, double step);

    /**
     * 给指定的key设定超时时间, 对已经有过期时间的key执行EXPIRE操作，将会更新它的过期时间
     * 文档: http://www.redis.cn/commands/expire.html
     *
     * @param expireTime 具体的超时时间
     * @param unit       时间单位
     * @return 设定是否成功
     */
    boolean expire(String key, long expireTime, TimeUnit unit);

    /**
     * 获取key的超时时间, 单位s
     */
    long ttl(String key);

    /**
     * 获取key的超时时间, 单位为毫秒 ms
     */
    long pttl(String key);

    /**
     * 移除key的超时时间, 使其持久化
     *
     * @return 移除是否成功, 如果该key没有超时时间, 则操作不成功, 返回false
     */
    boolean persist(String key);

    /**
     * 获取指定key的数据类型
     */
    String type(String key);

    /**
     * @return 在offset位置原来的值
     */
    boolean setBit(String key, long offset, boolean value);

    boolean getBit(String key, long offset);

    /**
     * 如果offset比当前key对应string还要长，那这个string前面补0以达到offset
     *
     * @return 修改之后的字符串长度
     */
    long setRange(String key, long offset, String value);

    String getRange(String key, long startOffset, long endOffset);

}
