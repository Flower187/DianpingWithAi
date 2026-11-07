package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;

import cn.hutool.core.lang.UUID;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.core.io.ClassPathResource;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class SimpleRedisLock implements ILock {

    /**
     * RedisTemplate
     */
    private StringRedisTemplate stringRedisTemplate;

   private static final String KEY_PREFIX="lock:";

    /**
     * ID前缀
     */
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";
    /**
     * 锁的名称
     */
    private String name;

    /**
     * 锁的有效期
     */
    public long timeoutSec;

    public SimpleRedisLock(StringRedisTemplate stringRedisTemplate, String name) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.name = name;
    }


    /**
     * 加载获取锁的Lua脚本
     */
    private static final DefaultRedisScript<Long> TRYLOCK_SCRIPT;

    static {
        TRYLOCK_SCRIPT = new DefaultRedisScript<>();
        TRYLOCK_SCRIPT.setLocation(new ClassPathResource("lua/trylock.lua"));
        TRYLOCK_SCRIPT.setResultType(Long.class);
    }


    @Override
    public boolean tryLock(long timeoutSec) {
        this.timeoutSec = timeoutSec;

        // 执行lua脚本
        Long result = stringRedisTemplate.execute(
                TRYLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId(),
                Long.toString(timeoutSec)
        );
        return result != null && result.equals(1L);
    }

    /**
     * 加载Lua脚本
     */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("lua/unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }


    @Override
    public void unLock() {
//执行lua

stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(KEY_PREFIX + name), ID_PREFIX + Thread.currentThread().getId(),Long.toString(timeoutSec));


    }
}
