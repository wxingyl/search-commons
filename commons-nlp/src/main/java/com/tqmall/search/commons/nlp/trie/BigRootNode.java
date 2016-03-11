package com.tqmall.search.commons.nlp.trie;

import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/1/29.
 * 字符较多的root节点
 */
public class BigRootNode<V> extends Node<V> {

    //子节点
    private final Node<?>[] children;

    private final char minChar;

    private final char maxChar;

    /**
     * @param minChar  最小的字符
     * @param capacity 容量
     */
    public BigRootNode(char minChar, int capacity) {
        super('\0', Status.NORMAL, null);
        this.minChar = minChar;
        long max = minChar + (long) capacity - 1;
        if (max > Character.MAX_VALUE) {
            throw new IllegalArgumentException("minChar: " + minChar + ", capacity: " + capacity
                    + " is so large, and maxChar: " + max + " greater then " + (int) Character.MAX_VALUE);
        }
        this.maxChar = (char) max;
        children = new Node[capacity];
    }

    @Override
    public boolean addChild(Node<V> node) {
        if (!isValidChar(node.c)) {
            throw new IllegalArgumentException("character '" + c + "' is not in the range of [ " + minChar + ',' + maxChar + ']');
        }
        Node<V> preNode;
        if ((preNode = getChild(node.c)) == null) {
            children[node.c - minChar] = node;
            return node.status == Status.LEAF_WORD;
        } else {
            return handleReplaceChildNode(preNode, node);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Node<V> getChild(char ch) {
        return isValidChar(ch) ? (Node<V>) children[ch - minChar] : null;
    }

    /**
     * 根节点的就算了~~~一直返回true
     *
     * @return 一直返回true
     */
    @Override
    public boolean haveChild() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    void childHandle(NodeChildHandle<V> handle) {
        for (Node<?> ch : children) {
            if (ch != null && ch.status != Status.DELETE) {
                if (!handle.onHandle((Node<V>) ch)) break;
            }
        }
    }

    private boolean isValidChar(char ch) {
        return !(ch < minChar || ch > maxChar);
    }

    @Override
    public List<Map.Entry<String, V>> allChildWords(String prefixKey) {
        throw new UnsupportedOperationException("root node can not been invoke allChildWords method");
    }

    /**
     * child大数组不置为null, 如果需要直接在外部将该对象置为null
     */
    @Override
    public void clear() {
        super.clear();
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                children[i].clear();
                children[i] = null;
            }
        }
    }
}
