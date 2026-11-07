package com.hmdp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建redis模板对象...");
        StringRedisTemplate stringredisTemplate = new StringRedisTemplate();
        //设置redis的连接工厂对象
        stringredisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis key的序列化器
        stringredisTemplate.setKeySerializer(new StringRedisSerializer());
        return stringredisTemplate;
    }
}
