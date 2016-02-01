package com.tqmall.search.commons.nlp.trie;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by xing on 16/1/28.
 * 匹配到的结果
 */
public class Hit<V> {
    /**
     * 模式串在母文本中的终止位置
     */
    private final int endPos;

    private String outputKey;

    private final V value;

    /**
     * 进来匹配的字符
     *
     * @param endPos    匹配到的结束位置
     * @param outputKey 匹配到的输出文本
     * @param value     对应节点的value
     */
    public Hit(int endPos, String outputKey, V value) {
        this.endPos = endPos;
        this.outputKey = outputKey;
        this.value = value;
    }

    public int getEndPos() {
        return endPos;
    }

    public V getValue() {
        return value;
    }

    public String getOutputKey() {
        return outputKey;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("endPos=").append(endPos).append(':').append(outputKey);
        if (value != null) {
            sb.append("; value:").append(value);
        }
        return sb.toString();
    }

    /**
     * 根据匹配到的node创建hits, 函数中对node.status, 即{@link AcNormalNode#accept()}不做判断
     *
     * @param endPos 匹配文本中的结束位置
     * @param node   匹配到的node, 其status不做判断
     * @param <V>    value泛型
     * @return hits
     */
    public static <V> List<Hit<V>> createHits(final int endPos, final AcNormalNode<V> node) {
        List<Hit<V>> ret = new LinkedList<>();
        ret.add(new Hit<>(endPos, node.getSingleOutput(), node.getValue()));
        if (node.getFailed() instanceof AcNormalNode) {
            AcNormalNode<V> failed = (AcNormalNode<V>) node.getFailed();
            if (failed.accept()) {
                ret.add(new Hit<>(endPos, failed.getSingleOutput(), failed.getValue()));
            }
        }
        return ret;
    }
}
