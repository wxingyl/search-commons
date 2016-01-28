package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/1/27.
 * 普通的node节点
 */
public class NormalNode<V> extends Node<V> {

    /**
     * 普通节点构造
     *
     * @param ch 对应字符
     */
    public NormalNode(char ch) {
        super(ch, NodeStatus.NORMAL, null);
    }

    /**
     * 叶子节点构造
     *
     * @param ch    对应字符
     * @param value 叶子节点对应的值
     */
    public NormalNode(char ch, V value) {
        super(ch, NodeStatus.LEAF_WORD, value);
    }

    @SuppressWarnings("unchecked")
    public boolean addChild(Node<V> node) {
        if (children == null) {
            children = new Node[1];
            children[0] = node;
            return true;
        }
        int index = binarySearch(children, node.c);
        if (index < 0) {
            index = -(index + 1);
            Node[] newChild = new Node[children.length + 1];
            if (index > 0) {
                System.arraycopy(children, 0, newChild, 0, index);
            }
            newChild[index] = node;
            if (children.length > index) {
                System.arraycopy(children, index, newChild, index + 1, children.length - index);
            }
            children = newChild;
            return true;
        } else {
            return handleReplaceChildNode((Node<V>) children[index], node);
        }
    }

    @SuppressWarnings("unchecked")
    public Node<V> getChild(char ch) {
        if (children == null) return null;
        int index = binarySearch(children, ch);
        return index < 0 ? null : (Node<V>) children[index];
    }

    @SuppressWarnings("unchecked")
    private void walkAppend(StringBuilder preKey, List<Map.Entry<String, V>> retList) {
        preKey.append(c);
        if (status == NodeStatus.LEAF_WORD || status == NodeStatus.WORD) {
            retList.add(new AbstractMap.SimpleEntry<>(preKey.toString(), value));
        }
        if (children != null) {
            final int startIndex = preKey.length();
            for (Node<?> c : children) {
                NormalNode<V> childNode = (NormalNode<V>) c;
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

}
