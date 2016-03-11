package com.tqmall.search.commons.trie;

import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.*;

/**
 * Created by xing on 16/1/27.
 * 普通的node节点
 */
public class NormalNode<V> extends Node<V> {

    /**
     * 默认数组扩展大小
     */
    static final int DEFAULT_INFLATE_SIZE = 16;

    protected int childCount;

    protected Node<?>[] children;

    /**
     * 普通节点构造
     *
     * @param ch 对应字符
     */
    public NormalNode(char ch) {
        this(ch, Status.NORMAL, null);
    }

    /**
     * 叶子节点构造
     *
     * @param ch    对应字符
     * @param value 叶子节点对应的值
     */
    public NormalNode(char ch, V value) {
        this(ch, Status.LEAF_WORD, value);
    }

    public NormalNode(char ch, Status status, V value) {
        super(ch, status, value);
    }

    /**
     * 扩展children数组, 每次扩展DEFAULT_INFLATE_SIZE
     */
    private void inflateChildrenArray() {
        if (children == null) {
            children = new Node[DEFAULT_INFLATE_SIZE];
        } else {
            Node[] newChildren = new Node[children.length + DEFAULT_INFLATE_SIZE];
            System.arraycopy(children, 0, newChildren, 0, children.length);
            children = newChildren;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addChild(Node<V> node) {
        //first child, simple handle
        if (children == null) {
            inflateChildrenArray();
            children[0] = node;
            childCount++;
            return true;
        }
        int index = binarySearch(children, 0, childCount, node.c);
        if (index < 0) {
            index = -(index + 1);
            if ((childCount + 1) >= children.length) {
                inflateChildrenArray();
            }
            if (index < childCount) {
                System.arraycopy(children, index, children, index + 1, childCount - index);
            }
            children[index] = node;
            childCount++;
            return true;
        } else {
            return handleReplaceChildNode((Node<V>) children[index], node);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Node<V> getChild(char ch) {
        if (children == null) return null;
        int index = binarySearch(children, 0, childCount, ch);
        return index < 0 ? null : (Node<V>) children[index];
    }

    @Override
    public boolean haveChild() {
        if (children == null) return false;
        for (int i = 0; i < childCount; i++) {
            if (children[i].status != Status.DELETE) return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void childHandle(NodeChildHandle<V> handle) {
        if (children == null) return;
        for (int i = 0; i < childCount; i++) {
            if (children[i].status != Status.DELETE) {
                if (!handle.onHandle((Node<V>) children[i])) break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void walkAppend(StringBuilder preKey, List<Map.Entry<String, V>> retList) {
        if (status == Status.DELETE) return;
        preKey.append(c);
        if (accept()) {
            retList.add(new AbstractMap.SimpleEntry<>(preKey.toString(), value));
        }
        if (children != null) {
            final int startIndex = preKey.length();
            for (int i = 0; i < childCount; i++) {
                NormalNode<V> childNode = (NormalNode<V>) children[i];
                childNode.walkAppend(preKey, retList);
                preKey.delete(startIndex, preKey.length());
            }
        }
    }

    @Override
    public List<Map.Entry<String, V>> allChildWords(String prefixKey) {
        if (SearchStringUtils.isEmpty(prefixKey)) return null;
        if (prefixKey.charAt(prefixKey.length() - 1) != c) {
            throw new IllegalArgumentException("the prefixKey: " + prefixKey + " last char isn't " + c);
        }
        List<Map.Entry<String, V>> retList = new ArrayList<>();
        walkAppend(new StringBuilder(prefixKey).deleteCharAt(prefixKey.length() - 1), retList);
        return retList;
    }

    @Override
    public void clear() {
        super.clear();
        for (int i = 0; i < childCount; i++) {
            children[i].clear();
            children[i] = null;
        }
        childCount = 0;
        children = null;
    }
}
