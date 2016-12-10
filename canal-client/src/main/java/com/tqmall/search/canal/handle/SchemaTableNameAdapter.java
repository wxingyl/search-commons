package com.tqmall.search.canal.handle;

import java.util.List;
import java.util.Map;

/**
 * date 2016/12/10 下午1:49
 * 数据库schema, table name适配, 分库分表可以实现该适配器
 *
 * @author 尚辰
 */
public interface SchemaTableNameAdapter {

    /**
     * 通过真实的库, 表名 获取虚拟的库, 表, 比如test库中的user表按照userId路由到test[0-3] 4个库, user[00-15] 16个表,
     * 通过 realSchema = 'test0', realTable = 'user00'拿到'test'和'table'
     * 通过 realSchema = 'test3', realTable = 'user15'拿到'test'和'table'
     *
     * @param realSchema 具体的某个schema name
     * @param realTable  具体的某个table name
     * @return 虚拟的表名, {@link Map.Entry#getKey()} schema, {@link Map.Entry#getValue()} table
     */
    Map.Entry<String, String> getVirtualName(String realSchema, String realTable);

    /**
     * 通过虚拟的库, 表名 获取对应的所有真实库, 表, 比如test库中的user表按照userId路由到test[0-1] 2个库, user[0-3] 4个表,
     * 通过 virtualSchema = 'test', virtualTable = 'user' 拿到
     * test0 --> [user00, user01]
     * test1 --> [user02, user03]
     * 当然这个具体的路由规则根据实现者自己制定, 上面只是一种路由情况
     *
     * @param virtualSchema 虚拟schema name
     * @param virtualTable  虚拟table name
     * @return 真实表名, {@link Map#keySet()} 真实的schema list, {@link Map#values()} 真实的table list
     */
    Map<String, List<String>> getRealName(String virtualSchema, String virtualTable);
}
