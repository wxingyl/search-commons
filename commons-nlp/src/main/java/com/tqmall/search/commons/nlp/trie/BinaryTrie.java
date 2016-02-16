package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/1/27.
 * CJK二分查找树, 根节点直接分配, 其他的根据需要再添加
 */
public class BinaryTrie<V> extends AbstractTrie<V> {

    private int size;

    public BinaryTrie(TrieNodeFactory<V> nodeFactory) {
        super(nodeFactory);
    }

    @Override
    public boolean put(String key, V value) {
        char[] charArray = argCheck(key);
        if (charArray == null) return false;
        Node<V> current = root;
        for (int i = 0; i < charArray.length - 1; i++) {
            Node<V> next = current.getChild(charArray[i]);
            if (next == null) {
                next = nodeFactory.createNormalNode(charArray[i]);
                current.addChild(next);
            }
            current = next;
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

    @Override
    public void clear() {
        root.clear();
    }

    public static char[] argCheck(String key) {
        return SearchStringUtils.isEmpty(key) ? null : key.toCharArray();
    }
}
