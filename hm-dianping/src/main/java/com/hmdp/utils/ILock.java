package com.hmdp.utils;

public interface ILock {
    /**
     * 获得锁
     * @param timeout
     * @return
     */
    boolean tryLock(long timeout) ;

    /**
     * 释放锁
     * @return
     */
    void unLock();
}
