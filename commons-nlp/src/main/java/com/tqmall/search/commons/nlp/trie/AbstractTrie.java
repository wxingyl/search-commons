package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.Objects;

/**
 * Created by xing on 16/1/27.
 * Trie抽象类
 */
public abstract class AbstractTrie<V> implements Trie<V> {

    protected final TrieNodeFactory<V> nodeFactory;

    protected final Node<V> root;

    public AbstractTrie(TrieNodeFactory<V> nodeFactory) {
        this.nodeFactory = nodeFactory;
        this.root = nodeFactory.createRootNode();
        Objects.requireNonNull(root);
    }

    /**
     * node节点搜索, 筛选掉DELETE的节点
     *
     * @return key无效或者节点已经被删除, 返回null
     */
    protected Node<V> searchNode(String key) {
        char[] charArray = argCheck(key);
        return charArray == null ? null : searchNode(charArray);
    }

    /**
     * 不做array 参数校验
     *
     * @param array 不做参数校验
     */
    protected Node<V> searchNode(char[] array) {
        Node<V> currentNode = root;
        for (char c : array) {
            currentNode = currentNode.getChild(c);
            if (currentNode == null || currentNode.getStatus() == Node.Status.DELETE) return null;
        }
        return currentNode;
    }

    public final TrieNodeFactory<V> getNodeFactory() {
        return nodeFactory;
    }

    public static char[] argCheck(String key) {
        return SearchStringUtils.isEmpty(key) ? null : key.toCharArray();
    }
}
