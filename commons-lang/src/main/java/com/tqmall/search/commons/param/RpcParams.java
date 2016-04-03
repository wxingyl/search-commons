package com.tqmall.search.commons.param;

import com.tqmall.search.commons.condition.ConditionContainer;

import java.util.Iterator;
import java.util.ServiceLoader;


/**
 * Created by xing on 16/4/2.
 * Rpc param工具类
 *
 * @author xing
 */
public final class RpcParams {

    /**
     * 优先通过{@link ServiceLoader}加载{@link SourceNameFactory}
     * 如果没有则加载默认的类: com.tqmall.search.commons.param.SourceNameFactoryImpl.class
     * 如果没有那就只能null了~~~
     */
    public final static String SOURCE;

    static {
        String source = null;
        Iterator<SourceNameFactory> it = ServiceLoader.load(SourceNameFactory.class).iterator();
        if (it.hasNext()) {
            source = it.next().sourceName();
        }
        if (source == null) {
            try {
                Class cls = Class.forName(SourceNameFactory.class.getName() + "Impl");
                SourceNameFactory factory = (SourceNameFactory) cls.newInstance();
                source = factory.sourceName();
            } catch (Throwable ignored) {
            }
        }
        SOURCE = source;
    }

    private RpcParams() {
    }

    /**
     * 降序
     *
     * @param field 需要排序的字段名
     * @return 降序sort
     */
    public static FieldSort descSort(String field) {
        return new FieldSort(field, false);
    }

    /**
     * 升序
     *
     * @param field 需要排序的字段名
     * @return 升序sort
     */
    public static FieldSort ascSort(String field) {
        return new FieldSort(field, true);
    }

    public static RpcParam newParam(ConditionContainer conditionContainer) {
        RpcParam param = new RpcParam();
        param.setConditionContainer(conditionContainer);
        return param;
    }

    public static RpcPageParam newPageParam(ConditionContainer conditionContainer) {
        RpcPageParam param = new RpcPageParam();
        param.setConditionContainer(conditionContainer);
        return param;
    }

    public static RpcPageParam newPageParam(int pageSize, ConditionContainer conditionContainer) {
        RpcPageParam param = new RpcPageParam(pageSize);
        param.setConditionContainer(conditionContainer);
        return param;
    }

    public static RpcKeywordParam newKeywordParam(String q) {
        return new RpcKeywordParam(q);
    }

    public static RpcKeywordParam newKeywordParam(String q, ConditionContainer conditionContainer) {
        RpcKeywordParam param = new RpcKeywordParam(q);
        param.setConditionContainer(conditionContainer);
        return param;
    }

    public static RpcKeywordParam newKeywordParam(String q, int pageSize) {
        return new RpcKeywordParam(q, pageSize);
    }

    public static RpcKeywordParam newKeywordParam(String q, int pageSize, ConditionContainer conditionContainer) {
        RpcKeywordParam param = new RpcKeywordParam(q, pageSize);
        param.setConditionContainer(conditionContainer);
        return param;
    }
}
