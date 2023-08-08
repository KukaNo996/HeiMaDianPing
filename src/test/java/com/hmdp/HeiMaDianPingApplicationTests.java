package com.hmdp;

import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class HeiMaDianPingApplicationTests {
    @Resource
    private ShopServiceImpl shopService;
    private static final ExecutorService CACHE_REBUILD_EXEUTOR = Executors.newFixedThreadPool(10);
    @Resource
    private RedisIdWorker redisIdWorker;

    @Test
    void contextLoads() throws InterruptedException {
        shopService.saveShopToRedis(1L, 10L);
    }

    @Test
    void testRedisIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println(id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            CACHE_REBUILD_EXEUTOR.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("消耗时长:" + (end - begin) + "ms");
    }

}
