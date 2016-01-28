package com.tqmall.search.commons.nlp.trie;

/**
 * Created by xing on 16/1/27.
 * 节点的状态
 */
public enum NodeStatus {
    //普通的,不是词的节点
    NORMAL,
    //词的结尾, 但不是叶子节点
    WORD,
    //叶子节点
    LEAF_WORD
}
