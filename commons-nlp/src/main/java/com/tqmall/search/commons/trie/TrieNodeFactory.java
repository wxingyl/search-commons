package com.tqmall.search.commons.trie;

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
     * 创建叶子节点, 其实该方法跟{@link #createNormalNode(char)}一样, 只是状态不一样而已
     *
     * @param c     对应字符
     * @param value 该叶子节点对用的value
     * @return 叶子节点对象
     */
    Node<V> createChildNode(char c, V value);
}
