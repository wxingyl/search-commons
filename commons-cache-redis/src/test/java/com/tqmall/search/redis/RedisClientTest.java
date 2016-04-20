package com.tqmall.search.redis;

import org.junit.*;
import redis.clients.jedis.JedisPool;

import java.io.Serializable;

/**
 * date 16/4/18 下午1:42
 *
 * @author 尚辰
 */
public class RedisClientTest {

    private static RedisClient redisClient;

    //    @BeforeClass
    @Ignore
    public static void init() {
        JedisPool jedisPool = new JedisPool("127.0.0.1", 6379);
        redisClient = new DefaultRedisClient<>(jedisPool);
    }

    //    @AfterClass
    @Ignore
    public static void destroy() {
        redisClient.close();
    }

    //    @Test
    @Ignore
    public void redisSet() {
        redisClient.set("name", "wxingyl");
        String name = redisClient.get("name", String.class);
        Assert.assertEquals("wxingyl", name);
        Bean bean = new Bean();
        bean.setId(12);
        bean.setName("wxingyl");
        redisClient.set("bean_test", bean);
        Bean readBean = redisClient.get("bean_test", Bean.class);
        Assert.assertEquals(bean, readBean);
    }

    public static class Bean implements Serializable {

        private static final long serialVersionUID = 1L;

        private Integer id;

        private String name;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Bean bean = (Bean) o;

            if (!id.equals(bean.id)) return false;
            return name.equals(bean.name);

        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }
    }
}
