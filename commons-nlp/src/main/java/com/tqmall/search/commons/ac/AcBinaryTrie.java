package com.tqmall.search.commons.ac;

import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.nlp.NlpUtils;
import com.tqmall.search.commons.trie.BinaryTrie;
import com.tqmall.search.commons.trie.Node;
import com.tqmall.search.commons.trie.NodeChildHandle;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xing on 16/1/28.
 * Aho-Corasick 模式匹配树, 论文: http://cr.yp.to/bib/1975/aho.pdf
 * 二分查找前缀树实现
 * 注: {@link #trie}的{@link BinaryTrie#getNodeFactory()} 必须是AcTrieNodeFactory的实例
 */
public class AcBinaryTrie<V> extends AbstractAcTrie<V> {

    /**
     * failed字段构造读写锁, 构造failed字段时不能执行匹配操作
     */
    private ReadWriteLock failedRwLock = new ReentrantReadWriteLock();

    private final Node<V> trieRoot;

    /**
     * @param trie 其nodeFactory必须是AcTrieNodeFactory的实例
     */
    public AcBinaryTrie(BinaryTrie<V> trie) {
        super(trie);
        if (!(trie.getNodeFactory() instanceof AcTrieNodeFactory)) {
            throw new IllegalArgumentException("the nodeFactory of binaryTrie must instanceof AcTrieNodeFactory");
        }
        trieRoot = trie.getRoot();
        initFailed();
    }

    @Override
    public boolean put(String key, V value) {
        failedRwLock.writeLock().lock();
        try {
            boolean added = super.put(key, value);
            AcNormalNode<V> node;
            if (added && (node = (AcNormalNode<V>) getNode(key)) != null) {
                node.setSingleOutput(key);
            }
            return added;
        } finally {
            failedRwLock.writeLock().unlock();
        }
    }

    /**
     * 初始化failed {@link AcNormalNode} failed等字段
     */
    @Override
    public void initFailed() {
        failedRwLock.writeLock().lock();
        try {
            final List<AcNormalNode<V>> rootChildNodes = new ArrayList<>();
            trieRoot.childHandle(new NodeChildHandle<V>() {
                @Override
                public boolean onHandle(final Node<V> child) {
                    AcNormalNode<V> acNode = (AcNormalNode<V>) child;
                    acNode.initRootChildNode(trieRoot);
                    rootChildNodes.add(acNode);
                    return true;
                }
            });
            for (AcNormalNode<V> acNode : rootChildNodes) {
                acNode.buildFailed(trieRoot);
            }
        } finally {
            failedRwLock.writeLock().unlock();
        }
    }

    @Override
    public final List<Hit<V>> match(char[] text, final int off, final int len) {
        final int endPos = off + len;
        NlpUtils.arrayIndexCheck(text, off, endPos);
        if (len == 0) return null;
        failedRwLock.readLock().lock();
        try {
            List<Hit<V>> hits = new LinkedList<>();
            Node<V> currentNode = trieRoot;
            int cursor = off;
            while (cursor < endPos) {
                AcNormalNode<V> nextNode = (AcNormalNode<V>) currentNode.getChild(text[cursor]);
                if (nextNode == null) {
                    if (currentNode == trieRoot) {
                        //当前节点已经是rootNode, 则不匹配
                        cursor++;
                    } else {
                        //当前节点不是rootNode, 可以尝试failed节点, 再来一次查找
                        currentNode = ((AcNormalNode<V>) currentNode).getFailed();
                        if (currentNode == null) currentNode = trieRoot;
                    }
                } else {
                    //匹配到了
                    cursor++;
                    if (nextNode.accept()) {
                        //匹配到, 将所有结果添加进来
                        hits.add(Hit.valueOf(cursor, nextNode));
                        if (nextNode.getFailed() instanceof AcNormalNode) {
                            AcNormalNode<V> failedNode = (AcNormalNode<V>) nextNode.getFailed();
                            if (failedNode.accept()) {
                                hits.add(Hit.valueOf(cursor, failedNode));
                            }
                        }
                    }
                    currentNode = nextNode;
                }
            }
            return hits;
        } finally {
            failedRwLock.readLock().unlock();
        }
    }

    public static <V> Builder<V> build() {
        return new Builder<>();
    }

    public static class Builder<V> {

        private TreeMap<String, V> dataMap = new TreeMap<>();

        public Builder<V> put(String key, V value) {
            dataMap.put(key, value);
            return this;
        }

        public Builder<V> putAll(Map<String, ? extends V> m) {
            dataMap.putAll(m);
            return this;
        }

        /**
         * trie默认使用{@link BinaryTrie}
         */
        public AcBinaryTrie<V> create(AcTrieNodeFactory<V> nodeFactory) {
            return create(new BinaryTrie<>(nodeFactory));
        }

        public AcBinaryTrie<V> create(BinaryTrie<V> binaryTrie) {
            Objects.requireNonNull(binaryTrie);
            for (Map.Entry<String, V> e : dataMap.entrySet()) {
                binaryTrie.put(e.getKey(), e.getValue());
            }
            return new AcBinaryTrie<>(binaryTrie);
        }

    }
}
