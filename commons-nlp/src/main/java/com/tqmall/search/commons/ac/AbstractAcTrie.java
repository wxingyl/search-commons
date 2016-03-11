package com.tqmall.search.commons.ac;

import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.trie.Node;
import com.tqmall.search.commons.trie.Trie;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by xing on 16/3/11.
 * AcTrie抽象封装, AcTrie中的数据都在{@link #trie}中, 所以相关put, size, update等都操作{@link #trie}
 *
 * @author xing
 */
public abstract class AbstractAcTrie<V> implements AcTrie<V> {

    private final Trie<V> trie;

    protected AbstractAcTrie(Trie<V> trie) {
        this.trie = trie;
    }

    @Override
    public boolean put(String key, V value) {
        return trie.put(key, value);
    }

    @Override
    public boolean remove(String word) {
        return trie.remove(word);
    }

    @Override
    public Node<V> getNode(String key) {
        return trie.getNode(key);
    }

    @Override
    public List<Map.Entry<String, V>> prefixSearch(String word) {
        return trie.prefixSearch(word);
    }

    @Override
    public final V getValue(String key) {
        return trie.getValue(key);
    }

    @Override
    public final boolean updateValue(String key, V value) {
        Node<V> node = trie.getNode(key);
        if (node == null || !node.accept()) return false;
        node.setValue(value);
        return true;
    }

    @Override
    public final List<Hit<V>> match(char[] text) {
        Objects.requireNonNull(text);
        return match(text, 0, text.length);
    }

    @Override
    public final int size() {
        return trie.size();
    }

    @Override
    public void clear() {
        trie.clear();
    }

}
