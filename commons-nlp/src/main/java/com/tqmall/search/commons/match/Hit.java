package com.tqmall.search.commons.match;

import com.tqmall.search.commons.ac.AcNormalNode;

/**
 * Created by xing on 16/1/28.
 * 匹配到的结果, 位置为[), 左开右闭
 */
public class Hit<V> implements Comparable<Hit<V>> {
    /**
     * 匹配到的开始位置
     */
    private int start;
    /**
     * 匹配结果的结束位置, 即最后一个字符的下一个位置
     */
    private int end;

    private V value;

    public Hit(int start, int end, V value) {
        this.start = start;
        this.end = end;
        this.value = value;
    }

    /**
     * 进来匹配的字符, only for test
     * 参数key没有什么用处, 只用用来求匹配字符的长度
     *
     * @param start 匹配到的开始位置, 符合左开右闭原则,即[startPos, endPos)
     * @param key   匹配到的输出文本
     * @param value 对应节点的value
     */
    public Hit(int start, String key, V value) {
        this.start = start;
        this.end = start + key.length();
        this.value = value;
    }

    public Hit(int endPos, AcNormalNode<V> acNode) {
        if (!acNode.accept()) {
            throw new IllegalArgumentException("acNode: " + acNode + " accept is false");
        }
        this.start = endPos - acNode.getSingleOutput().length();
        this.end = endPos;
        this.value = acNode.getValue();
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int length() {
        return end - start;
    }

    public void changePosition(int start, int end) {
        this.start = start;
        this.end = end;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(start).append(',').append(end);
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
        return start == hit.start && end == hit.end;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        return result;
    }

    @Override
    public int compareTo(Hit<V> o) {
        int cmp = Integer.compare(start, o.start);
        if (cmp == 0) {
            cmp = Integer.compare(end, o.end);
        }
        return cmp;
    }

}
