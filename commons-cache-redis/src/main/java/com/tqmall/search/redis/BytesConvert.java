package com.tqmall.search.redis;

/**
 * date 16/4/16 下午4:12
 * 对象转化为字节数组byte[] 给redis用
 *
 * @author 尚辰
 */
public interface BytesConvert<T> {

    /**
     * 转换成字节数组
     */
    byte[] toBytes(T obj);

    /**
     * 字节数组实例化成对象
     */
    T initBytes(byte[] bytes, Class<T> cls);
}
