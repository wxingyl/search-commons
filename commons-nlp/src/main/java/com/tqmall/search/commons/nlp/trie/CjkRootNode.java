package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.nlp.NlpConst;
import com.tqmall.search.commons.nlp.NlpUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/1/27.
 * Root根节点
 */
public class CjkRootNode<V> extends Node<V> {

    public CjkRootNode() {
        super('\0', NodeStatus.NORMAL, null);
        children = new NormalNode[NlpConst.CJK_UNIFIED_SIZE];
    }

    @Override
    public boolean addChild(Node<V> node) {
        if (!NlpUtils.isCjkChar(node.c)) {
            throw new IllegalArgumentException("字符: '" + c + "'不是CJK字符");
        }
        Node<V> preNode;
        if ((preNode = getChild(node.c)) == null) {
            children[node.c - NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST] = node;
            return true;
        } else {
            return handleReplaceChildNode(preNode, node);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Node<V> getChild(char ch) {
        return NlpUtils.isCjkChar(ch) ? (Node<V>) children[ch - NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST] : null;
    }

    @Override
    public List<Map.Entry<String, V>> allChildWords(String prefixKey) {
        return null;
    }

}
