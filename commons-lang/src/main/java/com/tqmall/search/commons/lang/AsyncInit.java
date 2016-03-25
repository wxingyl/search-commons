package com.tqmall.search.commons.lang;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Created by xing on 16/3/25.
 * 异步初始化工具类
 *
 * @author xing
 */
public class AsyncInit<T> implements Supplier<T> {

    private final Object lock = new Object();

    private T instance = null;

    /**
     * 单位毫秒, 等待初始化的超时时间, 默认100ms
     */
    private final long waitTimeOut;

    public AsyncInit(Executor executor, final Supplier<T> supplier) {
        this(executor, supplier, 100L);
    }

    /**
     * @param waitTimeOut 单位毫秒, 等待初始化的超时时间
     */
    public AsyncInit(Executor executor, final Supplier<T> supplier, long waitTimeOut) {
        Objects.requireNonNull(supplier);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    instance = supplier.get();
                    lock.notifyAll();
                }
            }
        });
        this.waitTimeOut = waitTimeOut > 0 ? waitTimeOut : 100L;
    }

    public final T getInstance() {
        if (instance == null) {
            synchronized (lock) {
                while (instance == null) {
                    try {
                        //顶多等100ms
                        lock.wait(waitTimeOut);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
        return instance;
    }

    @Override
    public final T get() {
        return getInstance();
    }
}
