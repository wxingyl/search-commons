package com.tqmall.search.commons.nlp.trie;

import java.util.List;

/**
 * Created by xing on 16/1/28.
 * Aho-Corasick 模式匹配树 接口定义
 * 接收态的值即为泛型V
 */
public interface AcTrie<V> {

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
     * 匹配字符串
     *
     * @param text 需要匹配的文本
     * @return 匹配结果
     */
    List<Hit<V>> textMatch(String text);

    int size();

    /**
     * 执行clear操作, 删除所有节点数据
     */
    void clear();
}
