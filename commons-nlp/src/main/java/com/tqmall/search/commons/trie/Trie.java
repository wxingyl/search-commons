package com.tqmall.search.commons.trie;


import java.util.List;
import java.util.Map;

/**
 * trie树
 */
public interface Trie<V> {

    /**
     * 添加词, 如果存在则更新
     *
     * @return 添加是否成功
     */
    boolean put(String key, V value);

    /**
     * 删除词
     *
     * @param key 要删除的词
     * @return 如果该词存在, 则删除
     */
    boolean remove(String key);

    /**
     * 获取指定字符串所在的节点, 排除删除的节点
     *
     * @return 不存在或者已经删除返回null
     */
    Node<V> getNode(String key);

    Node<V> getNode(char[] key, int off, int len);

    /**
     * 前缀查询, 如果查询的词不存在节点, 返回null
     *
     * @param word 要查询的词
     * @return 如果查询的词不存在节点, 返回null
     */
    List<Map.Entry<String, V>> prefixSearch(String word);

    /**
     * 当前前缀书加载的词条数目
     */
    int size();

    /**
     * 执行clear操作, 删除所有节点数据
     */
    void clear();
}
