package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Created by IntelliJ IDEA.
 *
 * @author ZhuShang
 * @PrjectName HeiMa_DianPing
 * @date 2023年08月08日 15:31
 * @Dercription
 */
@Component
public class RedisIdWorker {
    private static final long BEGIN_TIMESTAMP = 1672531260L;
    private static final int COUNT_BITS = 32;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String keyPrefix) {
        long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long timestamp = now - BEGIN_TIMESTAMP;
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        return timestamp << COUNT_BITS | count;
    }
}
