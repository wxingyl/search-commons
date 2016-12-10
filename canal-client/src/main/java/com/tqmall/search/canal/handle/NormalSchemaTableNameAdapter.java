package com.tqmall.search.canal.handle;

import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * date 2016/12/10 下午1:49
 * 没有分库分表的路由
 *
 * @author 尚辰
 */
public class NormalSchemaTableNameAdapter implements SchemaTableNameAdapter {

    public static final NormalSchemaTableNameAdapter INSTANCE = new NormalSchemaTableNameAdapter();

    NormalSchemaTableNameAdapter() {
    }

    @Override
    public Map.Entry<String, String> getVirtualName(String realSchema, String realTable) {
        return CommonsUtils.newImmutableMapEntry(realSchema, realTable);
    }

    @Override
    public Map<String, List<String>> getRealName(String virtualSchema, String virtualTable) {
        return Collections.singletonMap(virtualSchema, Collections.singletonList(virtualSchema));
    }
}
