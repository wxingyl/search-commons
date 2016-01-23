package com.tqmall.search.commons.utils;


import com.google.common.base.Supplier;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xing on 15/8/24.
 * read write lock util
 * jdk1.8使用的话很方便, 1.7上面就比较麻烦了~~~
 */
public class RwLock<T> {

    public static <T> RwLock<T> build(Supplier<T> supplier) {
        return new RwLock<>(supplier.get());
    }

    private ReadWriteLock rwLock;

    private T obj;

    public RwLock(T obj) {
        rwLock = new ReentrantReadWriteLock();
        this.obj = obj;
    }

    public void readOp(Op<T> op) {
        rwLock.readLock().lock();
        try {
            op.op(obj);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void writeOp(Op<T> op) {
        rwLock.writeLock().lock();
        try {
            op.op(obj);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public <R> R readOp(OpRet<T, R> operate) {
        rwLock.readLock().lock();
        try {
            return operate.op(obj);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public <R> R writeOp(OpRet<T, R> operate) {
        rwLock.writeLock().lock();
        try {
            return operate.op(obj);
        } finally {
            rwLock.writeLock().unlock();
        }
    }


    public interface Op<T> {

        void op(T input);
    }

    public interface OpRet<T, R> {

        R op(T input);
    }

}
