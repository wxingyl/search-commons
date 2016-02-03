package com.tqmall.search.commons.lang;

/**
 * Created by xing on 16/2/3.
 * 来加载工具类
 */
public class LazyInit<T> {

    private final Object lock = new Object();

    private volatile T instance;

    private final Supplier<T> supplier;

    public LazyInit(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = supplier.get();
                }
            }
        }
        return instance;
    }
}
