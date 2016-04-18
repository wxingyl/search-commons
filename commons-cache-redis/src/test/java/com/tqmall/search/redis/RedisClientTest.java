package com.tqmall.search.redis;

import org.junit.Test;

import java.util.BitSet;

/**
 * date 16/4/18 下午1:42
 *
 * @author 尚辰
 */
public class RedisClientTest {

    @Test
    public void bitSetTest() {
        BitSet bitSet = BitSet.valueOf(new long[]{8});
        System.out.println(bitSet);
    }
}
