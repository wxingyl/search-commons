package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by xing on 16/1/27.
 * CJK二分查找树, 根节点直接分配, 其他的根据需要再添加
 */
public class BinaryTrie<V> implements Trie<V> {

    private final TrieNodeFactory<V> nodeFactory;

    protected final Node<V> root;

    private int size;

    public BinaryTrie(TrieNodeFactory<V> nodeFactory) {
        this.nodeFactory = nodeFactory;
        this.root = nodeFactory.createRootNode();
        Objects.requireNonNull(root);
    }

    @Override
    public boolean put(String key, V value) {
        char[] charArray = argCheck(key);
        if (charArray == null) return false;
        Node<V> current = root;
        for (int i = 0; i < charArray.length - 1; i++) {
            current.addChild(nodeFactory.createNormalNode(charArray[i]));
            current = current.getChild(charArray[i]);
        }
        if (current.addChild(nodeFactory.createChildNode(charArray[charArray.length - 1], value))) {
            size++;
        }
        return true;
    }

    @Override
    public V getValue(String key) {
        Node<V> node = searchNode(key);
        return (node == null || node.getStatus() == Node.Status.NORMAL) ? null : node.getValue();
    }

    @Override
    public boolean remove(String word) {
        Node<V> node = searchNode(word);
        if (node == null || node.getStatus() == Node.Status.NORMAL) return false;
        char[] array = word.toCharArray();
        node = root;
        Node<V> startNode = root;
        int startIndex = 0;
        for (int i = 0; i < array.length; i++) {
            node = node.getChild(array[i]);
            if (node.getStatus() != Node.Status.NORMAL) {
                startIndex = i;
                startNode = node;
            }
        }
        startNode.removeNode(array, startIndex);
        size--;
        return true;
    }

    /**
     * node节点搜索, 筛选掉DELETE的节点
     *
     * @return key无效或者节点已经被删除, 返回null
     */
    private Node<V> searchNode(String key) {
        char[] charArray = argCheck(key);
        return charArray == null ? null : searchNode(charArray);
    }

    /**
     * 不做array 参数校验
     *
     * @param array 不做参数校验
     */
    private Node<V> searchNode(char[] array) {
        Node<V> currentNode = root;
        for (char c : array) {
            currentNode = currentNode.getChild(c);
            if (currentNode == null || currentNode.getStatus() == Node.Status.DELETE) return null;
        }
        return currentNode;
    }

    @Override
    public List<Map.Entry<String, V>> prefixSearch(String word) {
        Node<V> node = searchNode(word);
        if (node == null) return null;
        return node.allChildWords(word);
    }

    @Override
    public int size() {
        return size;
    }

    public TrieNodeFactory<V> getNodeFactory() {
        return nodeFactory;
    }

    public static char[] argCheck(String key) {
        return SearchStringUtils.isEmpty(key) ? null : key.toCharArray();
    }
}
