package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.nlp.trie.AcNormalNode;

import java.util.List;

/**
 * Created by xing on 16/1/28.
 * 匹配到的结果
 */
public class Hit<V> implements Comparable<Hit<V>> {

    private final int startPos;

    private final String key;

    private V value;

    public Hit(int startPos, String key) {
        this(startPos, key, null);
    }

    /**
     * 进来匹配的字符
     *
     * @param startPos 匹配到的开始位置, 符合左开右闭原则,即[startPos, endPos)
     * @param key      匹配到的输出文本
     * @param value    对应节点的value
     */
    public Hit(int startPos, String key, V value) {
        this.startPos = startPos;
        this.key = key;
        this.value = value;
    }

    public Hit(int endPos, AcNormalNode<V> acNode) {
        if (!acNode.accept()) {
            throw new IllegalArgumentException("acNode: " + acNode + " accept is false");
        }
        this.key = acNode.getSingleOutput();
        this.startPos = endPos - this.key.length();
        this.value = acNode.getValue();
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return startPos + key.length();
    }

    /**
     * 修改匹配结果的value
     */
    public void changeValue(V value) {
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(startPos).append(':').append(key);
        if (value != null) {
            sb.append(':').append(value);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hit)) return false;

        Hit<?> hit = (Hit<?>) o;
        if (startPos != hit.startPos) return false;
        return key.equals(hit.key);
    }

    @Override
    public int hashCode() {
        int result = startPos;
        result = 31 * result + key.hashCode();
        return result;
    }

    @Override
    public int compareTo(Hit<V> o) {
        int cmp = Integer.compare(startPos, o.startPos);
        if (cmp == 0) {
            cmp = Integer.compare(key.length(), o.key.length());
        }
        return cmp;
    }

    /**
     * 将匹配到的node添加到hits中, 函数中对node.status, 即{@link AcNormalNode#accept()}不做判断
     *
     * @param endPos 匹配文本中的结束位置
     * @param node   匹配到的node, 其status不做判断
     * @param <V>    value泛型
     */
    public static <V> void appendHits(List<Hit<V>> hits, final int endPos, final AcNormalNode<V> node) {
        hits.add(new Hit<>(endPos, node));
        if (node.getFailed() instanceof AcNormalNode) {
            AcNormalNode<V> failed = (AcNormalNode<V>) node.getFailed();
            if (failed.accept()) {
                hits.add(new Hit<>(endPos, failed));
            }
        }
    }
}
