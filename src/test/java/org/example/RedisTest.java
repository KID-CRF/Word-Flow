package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedis() {
        // 存
        redisTemplate.opsForValue().set("test:name", "wordflow");

        // 取
        Object value = redisTemplate.opsForValue().get("test:name");
        System.out.println("取出来的值：" + value);

        // ZSET 测试
        redisTemplate.opsForZSet().add("test:rank", "user1", 100);
        redisTemplate.opsForZSet().add("test:rank", "user2", 200);

        System.out.println("排行榜：" + redisTemplate.opsForZSet().reverseRange("test:rank", 0, -1));
    }
}
