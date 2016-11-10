package com.tqmall.search.commons.ac;

import com.tqmall.search.commons.match.AbstractTextMatch;
import com.tqmall.search.commons.trie.Node;
import com.tqmall.search.commons.trie.Trie;

import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/3/11.
 * AcTrie抽象封装, AcTrie中的数据都在{@link #trie}中, 所以相关put, size, update等都操作{@link #trie}
 *
 * @author xing
 */
public abstract class AbstractAcTrie<V> extends AbstractTextMatch<V> implements AcTrie<V> {

    private final Trie<V> trie;

    protected AbstractAcTrie(Trie<V> trie) {
        this.trie = trie;
    }

    @Override
    public boolean put(String key, V value) {
        return trie.put(key, value);
    }

    @Override
    public final boolean remove(String key) {
        throw  new UnsupportedOperationException();
    }

    @Override
    public final Node<V> getNode(String key) {
        return trie.getNode(key);
    }

    @Override
    public final Node<V> getNode(char[] key, int off, int len) {
        return trie.getNode(key, off, len);
    }

    @Override
    public final List<Map.Entry<String, V>> prefixSearch(String word) {
        return trie.prefixSearch(word);
    }

    @Override
    public final boolean updateValue(String key, V value) {
        Node<V> node = trie.getNode(key);
        if (node == null || !node.accept()) return false;
        node.setValue(value);
        return true;
    }

    @Override
    public final int size() {
        return trie.size();
    }

    @Override
    public void clear() {
        trie.clear();
    }

    @Override
    public Node<V> getRoot() {
        return trie.getRoot();
    }
}
