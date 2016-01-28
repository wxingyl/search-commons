package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/1/27.
 * CJK二分查找树, 根节点直接分配, 其他的根据需要再添加
 */
public class BinaryTrie<V> implements Trie<V> {

    private final Node<V> root;

    private int size;

    public BinaryTrie(Node<V> root) {
        this.root = root;
    }

    @Override
    public boolean put(String key, V value) {
        char[] charArray = argCheck(key);
        if (charArray == null) return false;
        Node<V> current = root;
        for (int i = 0; i < charArray.length - 1; i++) {
            current.addChild(new NormalNode<V>(charArray[i]));
            current = current.getChild(charArray[i]);
        }
        if (current.addChild(new NormalNode<>(charArray[charArray.length - 1], value))) {
            size++;
        }
        return true;
    }

    @Override
    public V getValue(String key) {
        Node<V> node = searchNode(key);
        return (node == null || node.status == NodeStatus.NORMAL) ? null : node.getValue();
    }

    private Node<V> searchNode(String key) {
        char[] charArray = argCheck(key);
        if (charArray == null) return null;
        Node<V> currentNode = root;
        final int length = key.length();
        for (int i = 0; i < length; i++) {
            currentNode = currentNode.getChild(key.charAt(i));
            if (currentNode == null) return null;
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

    private char[] argCheck(String key) {
        return SearchStringUtils.isEmpty(key) ? null : key.toCharArray();
    }
}
