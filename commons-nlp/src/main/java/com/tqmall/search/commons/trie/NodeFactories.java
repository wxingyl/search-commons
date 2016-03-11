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
public final class NodeFactories {

    private NodeFactories() {
    }

    /**
     * 获取默认实现的{@link TrieNodeFactory}对象
     */
    public static <V> TrieNodeFactory<V> defaultTrie(RootType type) {
        return new DefaultTrieNodeFactory<>(type);
    }

    /**
     * 获取默认实现的{@link AcTrieNodeFactory}对象
     */
    public static <V> AcTrieNodeFactory<V> defaultAcTrie(RootType type) {
        return new DefaultAcTrieNodeFactory<>(type);
    }

    static abstract class AbstractTrieNodeFactory<V> implements TrieNodeFactory<V> {

        protected final RootType rootType;

        protected AbstractTrieNodeFactory(RootType rootType) {
            this.rootType = rootType;
        }

        @Override
        public Node<V> createRootNode() {
            return rootType.createNode();
        }
    }

    static class DefaultTrieNodeFactory<V> extends AbstractTrieNodeFactory<V> {

        DefaultTrieNodeFactory(RootType rootType) {
            super(rootType);
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

        DefaultAcTrieNodeFactory(RootType rootType) {
            super(rootType);
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

    public enum RootType {
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

        RootType(Supplier<Node> supplier) {
            this.supplier = supplier;
        }

        @SuppressWarnings("unchecked")
        public <V> Node<V> createNode() {
            return supplier.get();
        }
    }

}
