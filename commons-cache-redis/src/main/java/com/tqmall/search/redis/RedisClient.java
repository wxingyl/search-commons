package com.tqmall.search.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * date 16/4/16 下午9:40
 * redis client操作接口定义
 * 类似{@link redis.clients.jedis.JedisCommands}, 只不过涉及到值相关的通过泛型T 来搞定
 * redis命令文档地址: http://redis.io/commands
 * 有个中文版: http://www.redis.cn/commands.html
 * 这儿没有实现所有的redis命令, 包括基本的指令, bit运算, hash操作和list操作指令. 其他如果需要, 通过{@link JedisTask}自己去实现吧
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
     * @see java.util.BitSet
     */
    boolean setBit(String key, long offset, boolean value);

    /**
     * @see java.util.BitSet
     */
    boolean getBit(String key, long offset);

    /**
     * 统计bit串中为1的位数, eg:
     * SET flag "\x00\xff\xf0"
     * BITCOUNT flag  ---> 返回 12
     *
     * @return 位数为1的数目
     * @see java.util.BitSet
     */
    long bitCount(String key);

    /**
     * 统计bit串指定区间中为1的位数, eg:
     * SET flag "\x00\xff\xf0"
     * BITCOUNT flag 1 1 ---> 返回 8
     * BITCOUNT flag 2 2 ---> 返回 4
     * BITCOUNT flag 1 2 ---> 返回 12
     * 注意: 这儿byteStart是以字节为单位的开始位置, 并不是bit的offset, 如上面的1就表示第2个字节位置, 也就是[8, 16)区间的bits
     *
     * @param byteStart 开始字节位置
     * @param byteEnd   结束字节位置
     * @return 指定区间中为1的位数
     * @see java.util.BitSet
     */
    long bitCount(String key, long byteStart, long byteEnd);

    /**
     * 获取bit串第一个匹配的bit位置, eg:
     * SET flag "\x00\xff\xf0"
     * BITPOS flag 1   ---> 返回 8
     * BITPOS flag 0   ---> 返回 0
     *
     * @param value true 匹配bit = 1的首位置, false 匹配bit = 0的首位置
     * @return 匹配到的bit位置, -1 表示不存在, 没有匹配
     * @see java.util.BitSet
     */
    long bitPos(String key, boolean value);

    /**
     * 获取bit串第一个匹配的bit位置, eg:
     * SET flag "\x00\xff\xf0"
     * BITPOS flag 1 1 2  ---> 返回 8
     * BITPOS flag 0 2 2  ---> 返回 20
     *
     * @param value     true 匹配bit = 1的首位置, false 匹配bit = 0的首位置
     * @param byteStart 开始字节位置
     * @param byteEnd   结束字节位置, 如果 < 0, 则表示不做结束位置限制
     * @return 匹配到的bit位置, -1 表示不存在, 没有匹配
     * @see java.util.BitSet
     */
    long bitPos(String key, boolean value, long byteStart, long byteEnd);

    /**
     * 如果offset比当前key对应string还要长，那这个string前面补0以达到offset
     *
     * @return 修改之后的字符串长度
     */
    long setRange(String key, long offset, String value);

    String getRange(String key, long startOffset, long endOffset);

    //hash 相关操作接口

    long hIncrBy(String key, String field, long value);

    double hIncrByFloat(String key, String field, double value);

    boolean hExist(String key, String field);

    /**
     * @return 删除是否成功
     */
    boolean hDel(String key, String... fields);

    long hLen(String key);

    Set<String> hKeys(String key);

    /**
     * @return true 表示添加, false 表示更新
     */
    <T> boolean hSet(String key, String field, T value);

    <T> T hGet(String key, String field, Class<T> valueCls);

    /**
     * 只有key.field不存在的时候设置
     *
     * @return true 表示设置成功, false 表示设置不成功, 原先已经存在了
     */
    <T> boolean hSetNx(String key, String field, T value);

    /**
     * 批量设定hash值
     *
     * @param key      key值
     * @param valueMap field value mapm
     * @return 设置是否成功
     */
    <T> boolean hmSet(String key, Map<String, T> valueMap);

    /**
     * 批量获取制定fields的值
     *
     * @param valueCls value的class type
     */
    <T> List<T> hmGet(String key, Class<T> valueCls, String... fields);

    <T> List<T> hVals(String key, Class<T> valueCls);

    <T> Map<String, T> hGetAll(String key, Class<T> valueCls);

    //list相关操作接口

    long lLen(String key);

    boolean lTrim(String key, long start, long end);

    /**
     * 往list尾插入元素
     *
     * @return 插入之后list的长度, 返回 -1 表示操作出错, 比如指定的key并不是list, 或者values为空
     */
    <T> long rPush(String key, T value);

    /**
     * 往list尾插入元素
     *
     * @return 插入之后list的长度, 返回 -1 表示操作出错, 比如指定的key并不是list, 或者values为空
     */
    <T> long rPush(String key, List<T> values);

    /**
     * 往list头插入元素
     *
     * @return 插入之后list的长度, 返回 -1 表示操作出错, 比如指定的key并不是list, 或者values为空
     */
    <T> long lPush(String key, T value);

    /**
     * 往list头插入元素
     *
     * @return 插入之后list的长度, 返回 -1 表示操作出错, 比如指定的key并不是list, 或者values为空
     */
    <T> long lPush(String key, List<T> values);

    <T> List<T> lRange(String key, Class<T> cls, long start, long end);

    <T> T lIndex(String key, Class<T> cls, long index);

    <T> boolean lSet(String key, long index, T value);

    /**
     * count > 0: 从头往尾移除值为 value 的元素
     * count < 0: 从尾往头移除值为 value 的元素
     * count = 0: 移除所有值为 value 的元素
     *
     * @param value 不能为null
     * @return 被移除的元素个数
     */
    <T> long lRem(String key, long count, T value);

    /**
     * 移除并返回存于 key 的 list 的第一个元素
     */
    <T> T lPop(String key, Class<T> valueCls);

    /**
     * 移除并返回存于 key 的 list 的最后一个元素
     */
    <T> T rPop(String key, Class<T> valueCls);

}
