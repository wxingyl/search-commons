package com.tqmall.search.commons.nlp.trie;

/**
 * Created by xing on 16/1/28.
 * Aho-Corasick 模式匹配树 接口定义
 * 接收态的值即为泛型V
 */
public interface AcTrie<V> extends TextMatch<V> {

    /**
     * 获取对应key的值
     *
     * @param key key
     * @return 如果key无效, 则返回null
     */
    V getValue(String key);

    /**
     * 更新key对应的value
     *
     * @return 是否成功执行了更新, 如果找到了合适的节点, 则更新成功[不做value是否与原先的有变化的判断]
     */
    boolean updateValue(String key, V value);

    /**
     * 构建failed字段
     */
    void initFailed();

    int size();

    /**
     * 执行clear操作, 删除所有节点数据
     */
    void clear();
}
