package com.dahaiwuliang.code;

import com.dahaiwuliang.code.util.Consts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@SpringBootTest
class CodeApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        List list = redisTemplate.opsForList().range("allArcType",0,10);
        System.out.println(list.size());
    }

}
