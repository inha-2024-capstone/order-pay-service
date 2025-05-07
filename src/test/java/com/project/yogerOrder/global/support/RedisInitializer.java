package com.project.yogerOrder.global.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@TestComponent
public class RedisInitializer {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    public void clear() {
        redisConnectionFactory.getConnection().serverCommands().flushAll();
    }

}
