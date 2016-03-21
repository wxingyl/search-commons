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

    public static <V> Hit<V> valueOf(int start, int end, V value) {
        return new Hit<>(start, end, value);
    }

    public static <V> Hit<V> valueOf(int endPos, AcNormalNode<V> acNode) {
        if (!acNode.accept()) {
            throw new IllegalArgumentException("acNode: " + acNode + " accept is false");
        }
        return new Hit<>(endPos - acNode.getSingleOutput().length(), endPos, acNode.getValue());
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
