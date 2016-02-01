package com.tqmall.search.commons.nlp.trie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by xing on 16/1/28.
 * Aho-Corasick 模式匹配树, 论文: http://cr.yp.to/bib/1975/aho.pdf
 * 二分查找树实现
 */
public class AcBinaryTrie<V> implements AcTrie<V> {

    private BinaryTrie<V> binaryTrie;

    public AcBinaryTrie(BinaryTrie<V> binaryTrie) {
        this.binaryTrie = binaryTrie;
    }

    @Override
    public V getValue(String key) {
        return binaryTrie.getValue(key);
    }

    @Override
    public boolean updateValue(String key, V value) {
        return binaryTrie.put(key, value);
    }

    @Override
    public List<Hit<V>> parseText(String text) {
        return null;
    }

    @Override
    public int size() {
        return binaryTrie.size();
    }

    public static <V> Builder<V> build() {
        return new Builder<>();
    }

    public static class Builder<V> {

        private TreeMap<String, V> dataMap = new TreeMap<>();
        /**
         * 默认rootNode为{@link LargeRootNode#createCjkRootNode()}
         * 子node为{@link AcNormalNode}
         */
        private AcTrieNodeFactory<V> nodeFactory;

        public Builder<V> nodeFactory(AcTrieNodeFactory<V> nodeFactory) {
            this.nodeFactory = nodeFactory;
            return this;
        }

        public Builder<V> put(String key, V value) {
            dataMap.put(key, value);
            return this;
        }

        public Builder<V> putAll(Map<String, ? extends V> m) {
            dataMap.putAll(m);
            return this;
        }

        public AcBinaryTrie<V> create() {
            if (nodeFactory == null) {
                nodeFactory = new AcTrieNodeFactory<V>() {
                    @Override
                    public AcNormalNode<V> createNormalNode(char c) {
                        return new AcNormalNode<>(c);
                    }

                    @Override
                    public AcNormalNode<V> createChildNode(char c, V value) {
                        return new AcNormalNode<>(c, value);
                    }

                    @Override
                    public Node<V> createRootNode() {
                        return LargeRootNode.createCjkRootNode();
                    }
                };
            }
            final BinaryTrie<V> binaryTrie = new BinaryTrie<>(nodeFactory);
            for (Map.Entry<String, V> e : dataMap.entrySet()) {
                binaryTrie.put(e.getKey(), e.getValue());
            }

            //初始化failed字段
            final List<AcNormalNode> rootChildNodes = new ArrayList<>();
            binaryTrie.root.childHandle(new NodeChildHandle() {
                @Override
                public boolean onHandle(final Node child) {
                    AcNormalNode acNode = (AcNormalNode) child;
                    acNode.initRootChildNode(binaryTrie.root);
                    rootChildNodes.add(acNode);
                    return true;
                }
            });
            for (AcNormalNode acNode : rootChildNodes) {
                acNode.buildFailed(binaryTrie.root);
            }
            return new AcBinaryTrie<>(binaryTrie);
        }
    }
}
