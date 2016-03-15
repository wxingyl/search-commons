package com.tqmall.search.commons.trie;

import com.tqmall.search.commons.nlp.NlpUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by xing on 16/1/27.
 * 二分查找树, 根节点直接分配, 其他的根据需要再添加
 */
public class BinaryTrie<V> implements Trie<V> {

    private final TrieNodeFactory<V> nodeFactory;

    protected final Node<V> root;

    private int size;

    public BinaryTrie(TrieNodeFactory<V> nodeFactory) {
        Objects.requireNonNull(nodeFactory);
        this.nodeFactory = nodeFactory;
        this.root = nodeFactory.createRootNode();
        Objects.requireNonNull(root);
    }

    /**
     * node节点搜索, 筛选掉DELETE的节点
     *
     * @return key无效或者节点已经被删除, 返回null
     */
    @Override
    public final Node<V> getNode(String key) {
        char[] charArray = NlpUtils.stringToCharArray(key);
        return charArray == null ? null : getNode(charArray);
    }

    /**
     * 不做array 参数校验
     *
     * @param array 不做参数校验
     */
    protected Node<V> getNode(char[] array) {
        Node<V> currentNode = root;
        for (char c : array) {
            currentNode = currentNode.getChild(c);
            if (currentNode == null || currentNode.getStatus() == Node.Status.DELETE) return null;
        }
        return currentNode;
    }

    @Override
    public boolean put(String key, V value) {
        return put(NlpUtils.stringToCharArray(key), value);
    }

    protected boolean put(char[] key, V value) {
        if (key == null || key.length == 0) return false;
        Node<V> current = root;
        for (int i = 0; i < key.length - 1; i++) {
            Node<V> next = current.getChild(key[i]);
            if (next == null) {
                next = nodeFactory.createNormalNode(key[i]);
                current.addChild(next);
            }
            current = next;
        }
        if (current.addChild(nodeFactory.createChildNode(key[key.length - 1], value))) {
            size++;
        }
        return true;
    }

    @Override
    public final V getValue(String key) {
        Node<V> node = getNode(key);
        return (node == null || node.getStatus() == Node.Status.NORMAL) ? null : node.getValue();
    }

    @Override
    public boolean remove(final String key) {
        //先确保这个词存在, 再执行删除, 这儿里面已经过滤了删除的节点
        char[] charArray = NlpUtils.stringToCharArray(key);
        if (charArray == null) return false;
        Node<V> node = getNode(charArray);
        //如果不是词节点, 返回
        if (node == null || node.getStatus() == Node.Status.NORMAL) return false;
        root.deleteNode(charArray, 0);
        size--;
        return true;
    }

    @Override
    public List<Map.Entry<String, V>> prefixSearch(String word) {
        char[] charArray = NlpUtils.stringToCharArray(word);
        if (charArray == null) return null;
        Node<V> node = getNode(charArray);
        if (node == null) return null;
        return node.allChildWords(charArray);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        root.clear();
    }

    public TrieNodeFactory<V> getNodeFactory() {
        return nodeFactory;
    }

    public Node<V> getRoot() {
        return root;
    }
}
