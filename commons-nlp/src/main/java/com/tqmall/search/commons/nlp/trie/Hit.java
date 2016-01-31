package com.tqmall.search.commons.nlp.trie;

/**
 * Created by xing on 16/1/28.
 * 匹配到的结果
 */
public class Hit<V> {
    /**
     * 模式串在母文本中的起始位置
     */
    private final int begin;
    /**
     * 模式串在母文本中的终止位置
     */
    private final int end;

    private final V value;

    public Hit(int begin, int end, V value) {
        this.begin = begin;
        this.end = end;
        this.value = value;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public V getValue() {
        return value;
    }
}
