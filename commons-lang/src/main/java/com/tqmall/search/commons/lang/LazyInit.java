package com.tqmall.search.commons.lang;

/**
 * Created by xing on 16/2/3.
 * 懒加载工具类
 */
public class LazyInit<T> implements Supplier<T> {

    private final Object lock = new Object();

    private volatile T instance;

    private final Supplier<T> supplier;

    public LazyInit(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public final T getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = supplier.get();
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
