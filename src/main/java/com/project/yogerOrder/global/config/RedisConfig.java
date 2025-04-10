package com.project.yogerOrder.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Bean
	public LettuceConnectionFactory lettuceConnectionFactory(RedisProperties properties) {
		RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration(properties.getHost(), properties.getPort());
		redisConfiguration.setPassword(properties.getPassword());

		return new LettuceConnectionFactory(redisConfiguration);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(lettuceConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		return redisTemplate;
	}

	@Bean
	public RedissonClient redissonClient(RedisProperties properties) {
		Config config = new Config();
		config.useSingleServer()
			.setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
			.setPassword(properties.getPassword());

		return Redisson.create(config);
	}
}
