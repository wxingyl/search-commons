package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.nlp.Hit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by xing on 16/1/28.
 * Aho-Corasick 模式匹配树, 论文: http://cr.yp.to/bib/1975/aho.pdf
 * 二分查找前缀树实现
 * 注: {@link #binaryTrie}的{@link BinaryTrie#getNodeFactory()} 必须是AcTrieNodeFactory的实例
 */
public class AcBinaryTrie<V> implements AcTrie<V> {

    private BinaryTrie<V> binaryTrie;

    /**
     * @param binaryTrie 其nodeFactory必须是AcTrieNodeFactory的实例
     */
    public AcBinaryTrie(BinaryTrie<V> binaryTrie) {
        if (!(binaryTrie.getNodeFactory() instanceof AcTrieNodeFactory)) {
            throw new IllegalArgumentException("the nodeFactory of binaryTrie must instanceof AcTrieNodeFactory");
        }
        this.binaryTrie = binaryTrie;
    }

    @Override
    public V getValue(String key) {
        return binaryTrie.getValue(key);
    }

    @Override
    public boolean updateValue(String key, V value) {
        Node<V> node = binaryTrie.searchNode(key);
        if (node == null || !node.accept()) return false;
        node.setValue(value);
        return true;
    }

    @Override
    public List<Hit<V>> textMatch(String text) {
        char[] charArray = BinaryTrie.argCheck(text);
        if (charArray == null) return null;
        List<Hit<V>> resultList = new ArrayList<>();
        Node<V> currentNode = binaryTrie.root;
        int cursor = 0;
        while (cursor < charArray.length) {
            final char ch = charArray[cursor];
            AcNormalNode<V> nextNode = (AcNormalNode<V>) currentNode.getChild(ch);
            if (nextNode == null) {
                if (currentNode == binaryTrie.root) {
                    //当前节点已经是rootNode, 则不匹配
                    cursor++;
                } else {
                    //当前节点不是rootNode, 可以尝试failed节点, 再来一次查找
                    currentNode = ((AcNormalNode<V>) currentNode).getFailed();
                }
            } else {
                //匹配到了
                cursor++;
                if (nextNode.accept()) {
                    //匹配到, 讲所有结果添加进来
                    resultList.addAll(Hit.createHits(cursor, nextNode));
                }
                currentNode = nextNode;
            }
        }
        return resultList;
    }

    @Override
    public int size() {
        return binaryTrie.size();
    }

    @Override
    public void clear() {
        binaryTrie.clear();
    }

    /**
     * 不能针对返回的节点进行添加或者删除操作, 或者如果执行了这些操作, 需要重新{@link #initFailed(BinaryTrie)}, 但是需要保证多线程
     * 读写安全
     */
    public BinaryTrie<V> getBinaryTrie() {
        return binaryTrie;
    }

    /**
     * 初始化failed {@link AcNormalNode} failed等字段
     *
     * @param binaryTrie 需要的二分前缀树
     * @param <V>        value泛型
     */
    public static <V> void initFailed(final BinaryTrie<V> binaryTrie) {
        final List<AcNormalNode<V>> rootChildNodes = new ArrayList<>();
        binaryTrie.root.childHandle(new NodeChildHandle<V>() {
            @Override
            public boolean onHandle(final Node<V> child) {
                AcNormalNode<V> acNode = (AcNormalNode<V>) child;
                acNode.initRootChildNode(binaryTrie.root);
                rootChildNodes.add(acNode);
                return true;
            }
        });
        for (AcNormalNode<V> acNode : rootChildNodes) {
            acNode.buildFailed(binaryTrie.root);
        }
    }

    public static <V> Builder<V> build() {
        return new Builder<>();
    }

    public static class Builder<V> {

        private TreeMap<String, V> dataMap = new TreeMap<>();
        /**
         * 默认rootNode为{@link LargeRootNode#createAsciiRootNode()} ()}
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
                        return LargeRootNode.createAsciiRootNode();
                    }
                };
            }
            final BinaryTrie<V> binaryTrie = new BinaryTrie<>(nodeFactory);
            for (Map.Entry<String, V> e : dataMap.entrySet()) {
                binaryTrie.put(e.getKey(), e.getValue());
            }

            //初始化failed字段
            initFailed(binaryTrie);
            return new AcBinaryTrie<>(binaryTrie);
        }
    }
}
