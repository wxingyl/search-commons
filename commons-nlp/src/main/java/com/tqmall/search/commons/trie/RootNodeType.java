package com.tqmall.search.commons.trie;

import com.tqmall.search.commons.lang.Supplier;
import com.tqmall.search.commons.nlp.NlpConst;
import com.tqmall.search.commons.ac.AcNormalNode;
import com.tqmall.search.commons.ac.AcTrieNodeFactory;

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

    static abstract class AbstractTrieNodeFactory<V> implements TrieNodeFactory<V> {

        protected final RootNodeType rootNodeType;

        protected AbstractTrieNodeFactory(RootNodeType rootNodeType) {
            this.rootNodeType = rootNodeType;
        }

        @Override
        public Node<V> createRootNode() {
            return rootNodeType.createRootNode();
        }
    }

    static class DefaultTrieNodeFactory<V> extends AbstractTrieNodeFactory<V> {

        DefaultTrieNodeFactory(RootNodeType rootNodeType) {
            super(rootNodeType);
        }

        @Override
        public Node<V> createNormalNode(char c) {
            return new NormalNode<>(c);
        }

        @Override
        public Node<V> createChildNode(char c, V value) {
            return new NormalNode<>(c, value);
        }

    }

    static class DefaultAcTrieNodeFactory<V> extends AbstractTrieNodeFactory<V>
            implements AcTrieNodeFactory<V> {

        DefaultAcTrieNodeFactory(RootNodeType rootNodeType) {
            super(rootNodeType);
        }

        @Override
        public AcNormalNode<V> createNormalNode(char c) {
            return new AcNormalNode<>(c);
        }

        @Override
        public AcNormalNode<V> createChildNode(char c, V value) {
            return new AcNormalNode<>(c, value);
        }

    }

    private final Supplier<Node> supplier;

    RootNodeType(Supplier<Node> supplier) {
        this.supplier = supplier;
    }

    @SuppressWarnings("unchecked")
    public <V> Node<V> createRootNode() {
        return supplier.get();
    }

    public <V> TrieNodeFactory<V> defaultTrie() {
        return new DefaultTrieNodeFactory<>(this);
    }

    public <V> AcTrieNodeFactory<V> defaultAcTrie() {
        return new DefaultAcTrieNodeFactory<>(this);
    }

}
