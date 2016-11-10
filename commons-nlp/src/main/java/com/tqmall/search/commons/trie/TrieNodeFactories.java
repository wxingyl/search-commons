package com.tqmall.search.commons.trie;

import com.tqmall.search.commons.ac.AcNormalNode;
import com.tqmall.search.commons.ac.AcTrieNodeFactory;

/**
 * Created by xing on 16/3/18.
 * {@link TrieNodeFactory}和{@link AcTrieNodeFactory}相关utils方法
 *
 * @author xing
 */
public final class TrieNodeFactories {

    private TrieNodeFactories() {
    }

    /**
     * 默认的Trie树节点生成器, 所有的叶子节点, 普通节点都是{@link NormalNode}
     */
    static class Trie<V> implements TrieNodeFactory<V> {

        private final Node<V> root;

        Trie(Node<V> root) {
            this.root = root;
        }

        @Override
        public Node<V> createRootNode() {
            return root;
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

    /**
     * 默认的AcTrie树节点生成器, 所有的叶子节点, 普通节点都是{@link AcNormalNode}
     */
    static class AcTrie<V> implements AcTrieNodeFactory<V> {

        private final Node<V> root;

        AcTrie(Node<V> root) {
            this.root = root;
        }

        @Override
        public Node<V> createRootNode() {
            return root;
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

    /**
     * @param root 创建好的root节点对象
     * @param <V>  Node对应泛型
     * @return 默认的 {@link TrieNodeFactory}实例
     */
    public static <V> TrieNodeFactory<V> defaultTrie(Node<V> root) {
        return new Trie<>(root);
    }

    /**
     * @param root 创建好的root节点对象
     * @param <V>  Node对应泛型
     * @return 默认的 {@link AcTrieNodeFactory}实例
     */
    public static <V> AcTrieNodeFactory<V> defaultAcTrie(Node<V> root) {
        return new AcTrie<>(root);
    }
}
