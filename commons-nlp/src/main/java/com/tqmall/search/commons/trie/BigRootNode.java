package com.tqmall.search.commons.trie;

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
        int max = capacity + minChar - 1;
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
            throw new IllegalArgumentException("character '" + c + "', \\u" + Integer.toHexString(c) + " is not in the range of [ " + minChar + ',' + maxChar + ']');
        }
        Node<V> preNode;
        if ((preNode = getChild(node.c)) == null) {
            children[node.c - minChar] = node;
            return node.status == Status.LEAF_WORD;
        } else {
            return handleReplaceChildNode(preNode, node);
        }
    }

    /**
     * RootNode 执行删除节点
     *
     * @return 是否中断删除操作
     */
    @Override
    public final boolean deleteNode(char[] word, int deep) {
        if (deep != 0) {
            throw new IllegalArgumentException("RootNode deep should equals 0");
        }
        Node childNode = getChild(word[0]);
        return childNode == null || childNode.deleteNode(word, 1);
    }

    @SuppressWarnings({"rawstype", "unchecked"})
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

    @Override
    @SuppressWarnings({"rawstype", "unchecked"})
    public void childHandle(NodeChildHandle<V> handle) {
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
    public List<Map.Entry<String, V>> allChildWords(char[] prefixKey) {
        throw new UnsupportedOperationException("root node can not been invoke allChildWords method");
    }

    /**
     * child大数组不置为null, 如果需要直接在外部将该对象置为null
     */
    @Override
    public final void clear() {
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                children[i].clear();
                children[i] = null;
            }
        }
    }

    @Override
    public void trimChildNodes() {
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                children[i].trimChildNodes();
            }
        }
    }

    private final static int HASH_CODE_FACTOR = BigRootNode.class.getSimpleName().hashCode();

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + HASH_CODE_FACTOR;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BigRootNode && super.equals(o);
    }
}
