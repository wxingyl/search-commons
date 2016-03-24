package com.tqmall.search.commons.ac;

import com.tqmall.search.commons.trie.TrieNodeFactory;

/**
 * Created by xing on 16/1/28.
 * Node节点生成接口
 */
public interface AcTrieNodeFactory<V> extends TrieNodeFactory<V> {

    /**
     * 创建普通的非根节点
     */
    AcNormalNode<V> createNormalNode(char c);

    /**
     * 创建叶子节点
     *
     * @param c     对应字符
     * @param value 该叶子节点对用的value
     * @return 叶子节点对象
     */
    AcNormalNode<V> createChildNode(char c, V value);
}
