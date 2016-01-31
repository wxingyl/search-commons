package com.tqmall.search.commons.nlp.node;

/**
 * Created by xing on 16/1/28.
 * Node节点生成接口
 */
public interface TrieNodeFactory<V> {

    /**
     * 创建根节点
     */
    Node<V> createRootNode();

    /**
     * 创建普通的非根节点
     */
    Node<V> createNormalNode(char c);

    /**
     * 创建叶子节点
     *
     * @param c     对应字符
     * @param value 该叶子节点对用的value
     * @return 叶子节点对象
     */
    Node<V> createChildNode(char c, V value);
}
