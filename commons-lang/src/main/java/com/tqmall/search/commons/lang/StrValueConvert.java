package com.tqmall.search.commons.lang;

/**
 * Created by xing on 15/10/24
 * 字符串转化为对应T类型
 * 定义该接口, 一方面简化字符串与各个类型转化, 另一方面主要是提供函数式编程方便, 其可以作为参数提供给接口,方便接口的实现
 */
public interface StrValueConvert<T> {

    T convert(String str);

}
