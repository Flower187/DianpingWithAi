package com.hmdp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@SpringBootTest
public class RedisConnectionTest {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Test
    public void testRedisConnection(){
        try(RedisConnection connection=redisConnectionFactory.getConnection()){
            String result=connection.ping();
            System.out.println("Redis连接测试: " + result);
            if(result.equals("PONG")){
                System.out.println("✅ Redis连接成功！");
            } else {
                System.out.println("❌ Redis连接异常");
            }
        }catch (Exception e) {
            System.out.println("Redis连接失败");
            e.printStackTrace();
           }
        }
    }

