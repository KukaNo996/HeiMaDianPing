package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import com.hmdp.service.ILock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 *
 * @author ZhuShang
 * @PrjectName HeiMa_DianPing
 * @date 2023年08月25日 8:51
 * @Dercription
 */
public class SimpleRedisLock implements ILock {
    public static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    private static final String KEY_PREFIX = "lock:";
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
    }

    private String name;
    private StringRedisTemplate stringRedisTemplate;
    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        String thingId = ID_PREFIX + String.valueOf(Thread.currentThread().getId());
        Boolean lockStatus = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, thingId, timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(lockStatus);
    }

    @Override
    public void unlock() {
        stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(KEY_PREFIX + name), ID_PREFIX + Thread.currentThread().getId());
    }

//    @Override
//    public void unlock() {
//        String thingId = ID_PREFIX+String.valueOf(Thread.currentThread().getId());
//        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
//        if (thingId.equals(id)){
//            stringRedisTemplate.delete(KEY_PREFIX+name);
//        }
//    }
}
