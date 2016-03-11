package com.tqmall.search.commons.nlp.trie;


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
     * @param word 要删除的词
     * @return 如果该词存在, 则删除
     */
    boolean remove(String word);

    V getValue(String key);

    Node<V> getNode(String key);

    /**
     * 前缀查询
     *
     * @param word 要查询的词
     * @return 如果没有匹配, 返回null
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
