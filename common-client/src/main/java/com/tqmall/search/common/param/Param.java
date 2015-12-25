package com.tqmall.search.common.param;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xing on 15/12/5.
 * 参数抽象父类
 */
public abstract class Param implements Serializable {
    /**
     * 记录系统调用来源
     */
    private String source;
    /**
     * 请求用来表示用户唯一的参数, 这个用户具体业务中区分用户的id, 比如电商的userId, UC的shopId等
     */
    private Integer uid;

    public String getSource() {
        return source;
    }

    public Integer getUid() {
        return uid;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    /**
     * 过滤掉值为null的value
     * 参数建议使用List.
     * Set, Map等开销较大, 不建议使用, 如果需要去重, 可以自己手动处理
     */
    public static <T> List<T> filterNullValue(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            if (it.next() == null) it.remove();
        }
        return list.isEmpty() ? null : list;
    }

    /**
     * 过滤String, 返回的String是trim过的
     * 关键字不能为null, 不能为空, 并且trim后不能为空
     * @return 返回的String是trim过的
     */
    public static String filterString(String q) {
        return (q != null && !q.isEmpty() && !(q = q.trim()).isEmpty()) ? q : null;
    }
}
