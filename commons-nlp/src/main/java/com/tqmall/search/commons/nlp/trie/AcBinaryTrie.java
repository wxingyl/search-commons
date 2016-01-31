package com.tqmall.search.commons.nlp.trie;

import java.util.List;

/**
 * Created by xing on 16/1/28.
 * Aho-Corasick 模式匹配树, 论文: http://cr.yp.to/bib/1975/aho.pdf
 * 二分查找树实现
 */
public class AcBinaryTrie<V> implements AcTrie<V> {

    private Trie<V> trie;

    public AcBinaryTrie(Trie<V> trie) {
        this.trie = trie;
    }

    @Override
    public V getValue(String key) {
        return trie.getValue(key);
    }

    @Override
    public boolean updateValue(String key, V value) {
        return trie.put(key, value);
    }

    @Override
    public List<Hit<V>> parseText(String text) {
        return null;
    }


    protected void buildFailed() {

    }

    @Override
    public int size() {
        return trie.size();
    }

    public static class Builder<V> {


    }
}
