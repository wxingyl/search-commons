package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.nlp.Hit;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xing on 16/1/28.
 * Aho-Corasick 模式匹配树, 论文: http://cr.yp.to/bib/1975/aho.pdf
 * 二分查找前缀树实现
 * 注: {@link #trie}的{@link BinaryTrie#getNodeFactory()} 必须是AcTrieNodeFactory的实例
 */
public class AcBinaryTrie<V> implements AcTrie<V> {

    /**
     * failed字段构造读写锁, 构造failed字段时不能执行匹配操作
     */
    private ReadWriteLock failedRwLock = new ReentrantReadWriteLock();

    private AbstractTrie<V> trie;

    /**
     * @param trie 其nodeFactory必须是AcTrieNodeFactory的实例
     */
    public AcBinaryTrie(AbstractTrie<V> trie) {
        if (!(trie.getNodeFactory() instanceof AcTrieNodeFactory)) {
            throw new IllegalArgumentException("the nodeFactory of binaryTrie must instanceof AcTrieNodeFactory");
        }
        this.trie = trie;
        initFailed();
    }

    @Override
    public V getValue(String key) {
        return trie.getValue(key);
    }

    @Override
    public boolean updateValue(String key, V value) {
        Node<V> node = trie.searchNode(key);
        if (node == null || !node.accept()) return false;
        node.setValue(value);
        return true;
    }

    /**
     * 初始化failed {@link AcNormalNode} failed等字段
     */
    @Override
    public void initFailed() {
        failedRwLock.writeLock().lock();
        try {
            final List<AcNormalNode<V>> rootChildNodes = new ArrayList<>();
            trie.root.childHandle(new NodeChildHandle<V>() {
                @Override
                public boolean onHandle(final Node<V> child) {
                    AcNormalNode<V> acNode = (AcNormalNode<V>) child;
                    acNode.initRootChildNode(trie.root);
                    rootChildNodes.add(acNode);
                    return true;
                }
            });
            for (AcNormalNode<V> acNode : rootChildNodes) {
                acNode.buildFailed(trie.root);
            }
        } finally {
            failedRwLock.writeLock().unlock();
        }
    }

    /**
     * @param text 文本数组
     * @return 匹配结果
     */
    @Override
    public List<Hit<V>> match(char[] text) {
        Objects.requireNonNull(text);
        return match(text, 0, text.length);
    }

    @Override
    public final List<Hit<V>> match(char[] text, final int startPos, final int length) {
        final int endPos = startPos + length;
        if (text == null || startPos < 0 || startPos > endPos) {
            throw new ArrayIndexOutOfBoundsException("text.length: " + (text == null ? 0 : text.length) + ", startPos: "
                    + startPos + ", endPos: " + endPos);
        }
        if (length == 0) return null;
        failedRwLock.readLock().lock();
        try {
            List<Hit<V>> hits = new ArrayList<>();
            Node<V> currentNode = trie.root;
            int cursor = startPos;
            while (cursor < endPos) {
                AcNormalNode<V> nextNode = (AcNormalNode<V>) currentNode.getChild(text[cursor]);
                if (nextNode == null) {
                    if (currentNode == trie.root) {
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
                        //匹配到, 将所有结果添加进来
                        Hit.appendHits(hits, cursor, nextNode);
                    }
                    currentNode = nextNode;
                }
            }
            return hits;
        } finally {
            failedRwLock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        return trie.size();
    }

    @Override
    public void clear() {
        trie.clear();
    }

    /**
     * 不能针对返回的节点进行添加或者删除操作, 或者如果执行了这些操作, 需要重新{@link #initFailed()}
     */
    public AbstractTrie<V> getTrie() {
        return trie;
    }

    public static <V> Builder<V> build() {
        return new Builder<>();
    }

    public static class Builder<V> {

        private TreeMap<String, V> dataMap = new TreeMap<>();
        /**
         * 默认rootNode为{@link BigRootNode#createAsciiRootNode()} ()}
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

        public AcBinaryTrie<V> create(Function<AcTrieNodeFactory<V>, AbstractTrie<V>> binaryTrieFactory) {
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
                        return BigRootNode.createAsciiRootNode();
                    }
                };
            }
            AbstractTrie<V> binaryTrie;
            if (binaryTrieFactory == null) {
                /**
                 * 默认砸门就构造{@link BinaryTrie}
                 */
                binaryTrie = new BinaryTrie<>(nodeFactory);
            } else {
                binaryTrie = binaryTrieFactory.apply(nodeFactory);
            }
            Objects.requireNonNull(binaryTrie);
            for (Map.Entry<String, V> e : dataMap.entrySet()) {
                binaryTrie.put(e.getKey(), e.getValue());
            }
            return new AcBinaryTrie<>(binaryTrie);
        }

        public AcBinaryTrie<V> create() {
            return create(null);
        }
    }
}
