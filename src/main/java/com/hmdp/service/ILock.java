package com.hmdp.service;

/**
 * Created by IntelliJ IDEA.
 *
 * @author ZhuShang
 * @PrjectName HeiMa_DianPing
 * @date 2023年08月25日 8:49
 * @Dercription
 */
public interface ILock {
    boolean tryLock(long timeoutSec);

    void unlock();
}
