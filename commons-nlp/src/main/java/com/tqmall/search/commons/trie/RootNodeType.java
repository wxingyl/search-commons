package com.tqmall.search.commons.trie;

import com.tqmall.search.commons.ac.AcTrieNodeFactory;
import com.tqmall.search.commons.lang.Supplier;
import com.tqmall.search.commons.nlp.NlpConst;

/**
 * Created by xing on 16/3/11.
 * {@link TrieNodeFactory}, {@link AcTrieNodeFactory}工具类
 *
 * @author xing
 * @see TrieNodeFactory
 * @see AcTrieNodeFactory
 */
public enum RootNodeType {
    NORMAL(new Supplier<Node>() {
        @Override
        public Node get() {
            return new NormalNode('\0');
        }
    }),
    ASCII(new Supplier<Node>() {
        @Override
        public Node get() {
            return new BigRootNode<>(Character.MIN_VALUE, 0xFF);
        }
    }),
    CJK(new Supplier<Node>() {
        @Override
        public Node get() {
            return new BigRootNode<>(NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST, NlpConst.CJK_UNIFIED_SIZE);
        }
    }),
    ALL(new Supplier<Node>() {
        @Override
        public Node get() {
            return new BigRootNode<>(Character.MIN_VALUE, Character.MAX_VALUE);
        }
    });

    private final Supplier<Node> supplier;

    RootNodeType(Supplier<Node> supplier) {
        this.supplier = supplier;
    }

    @SuppressWarnings("unchecked")
    public <V> Node<V> createRootNode() {
        return supplier.get();
    }

    public <V> TrieNodeFactory<V> defaultTrie() {
        return TrieNodeFactories.defaultTrie(this.<V>createRootNode());
    }

    public <V> AcTrieNodeFactory<V> defaultAcTrie() {
        return TrieNodeFactories.defaultAcTrie(this.<V>createRootNode());
    }

}
