package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.nlp.Hit;

import java.util.*;

/**
 * Created by xing on 16/2/20.
 * 字符串匹配器, 将算法抽取出来, 与数据隔离
 */
public abstract class TextMatcher<V> implements TextMatch<V> {

    protected final Node<V> root;

    protected TextMatcher(Node<V> root) {
        this.root = root;
    }

    /**
     * 具体匹配实现
     *
     * @param text 待匹配的字符数组
     * @return 匹配结果, 如果返回的list认为错误,整个文本处理结果返回null
     */
    protected abstract Collection<Hit<V>> runMatch(char[] text, int startPos, int endPos);

    /**
     * 匹配结果处理
     */
    protected abstract List<Hit<V>> hitsResultHandle(char[] text, int startPos, int endPos, Collection<Hit<V>> collection);

    /**
     * 文本匹配
     */
    @Override
    public final List<Hit<V>> match(char[] text) {
        Objects.requireNonNull(text);
        return match(text, 0, text.length);
    }

    @Override
    public final List<Hit<V>> match(char[] text, int startPos, int length) {
        final int endPos = startPos + length;
        if (text == null || startPos < 0 || startPos > endPos) {
            throw new ArrayIndexOutOfBoundsException("text.length: " + (text == null ? 0 : text.length) + ", startPos: "
                    + startPos + ", endPos: " + endPos);
        }
        if (length == 0) return null;
        Collection<Hit<V>> list = runMatch(text, startPos, endPos);
        if (list == null) return null;
        return hitsResultHandle(text, startPos, endPos, list);
    }

    public static <V> TextMatcher<V> minMatcher(Node<V> root) {
        return new MinBackTextMatcher<>(root);
    }

    public static <V> TextMatcher<V> maxMatcher(Node<V> root) {
        return new MaxBackTextMatcher<>(root);
    }

    /**
     * 反向最小匹配
     */
    public static class MinBackTextMatcher<V> extends TextMatcher<V> {

        protected MinBackTextMatcher(Node<V> root) {
            super(root);
        }

        @Override
        protected Collection<Hit<V>> runMatch(char[] text, int startPos, int endPos) {
            List<Hit<V>> hits = new ArrayList<>();
            Node<V> currentNode = root;
            int i = endPos - 1, matchStartPos = endPos, lastPos = endPos;
            while (i >= startPos) {
                Node<V> nextNode = i < lastPos ? currentNode.getChild(text[i]) : null;
                if (nextNode == null || nextNode.getStatus() == Node.Status.DELETE) {
                    //没有匹配到, 向前移
                    if (currentNode == root) {
                        i--;
                    } else {
                        i = matchStartPos - 1;
                        currentNode = root;
                    }
                } else {
                    //startPos只会不断减小
                    if (i < matchStartPos) matchStartPos = i;
                    i++;
                    if (nextNode.accept()) {
                        //匹配到一个词了~~~
                        hits.add(new Hit<>(i, new String(text, matchStartPos, i - matchStartPos), nextNode.getValue()));
                        i = matchStartPos - 1;
                        lastPos = matchStartPos;
                        currentNode = root;
                    } else {
                        currentNode = nextNode;
                    }
                }
            }
            return hits;
        }

        @Override
        protected final List<Hit<V>> hitsResultHandle(char[] text, int startPos, int endPos, Collection<Hit<V>> collection) {
            List<Hit<V>> hits = (List<Hit<V>>) collection;
            if (!hits.isEmpty()) {
                Collections.sort(hits);
            }
            return hits;
        }
    }


    /**
     * 反向最大匹配
     */
    public static class MaxBackTextMatcher<V> extends TextMatcher<V> {

        protected MaxBackTextMatcher(Node<V> root) {
            super(root);
        }

        private void addHitToMap(Hit<V> hit, Map<Integer, Hit<V>> hitsEndPosMap) {
            int end = hit.getEndPos(), start = hit.getStartPos();
            hitsEndPosMap.put(end, hit);
            while (--end > start) {
                hitsEndPosMap.remove(end);
            }
        }

        @Override
        protected Collection<Hit<V>> runMatch(char[] text, final int startPos, final int endPos) {
            Node<V> currentNode = root;
            int i = endPos - 1, hitStartPos = endPos, hitEndPos = endPos;
            boolean lastAccept = false;
            V hitValue = null;
            Map<Integer, Hit<V>> hitsEndPosMap = new TreeMap<>();
            while (i >= startPos) {
                Node<V> nextNode = i < text.length ? currentNode.getChild(text[i]) : null;
                if (nextNode == null || nextNode.getStatus() == Node.Status.DELETE) {
                    //没有匹配到, 向前移
                    if (currentNode == root) {
                        i--;
                    } else {
                        if (lastAccept) {
                            //匹配到一个词了~~~
                            addHitToMap(new Hit<>(hitEndPos, new String(text, hitStartPos,
                                    hitEndPos - hitStartPos), hitValue), hitsEndPosMap);
                            lastAccept = false;
                        }
                        i = hitStartPos - 1;
                        currentNode = root;
                    }
                } else {
                    //startPos只会不断减小
                    if (i < hitStartPos) hitStartPos = i;
                    i++;
                    if (nextNode.accept()) {
                        lastAccept = true;
                        hitEndPos = i;
                        hitValue = nextNode.getValue();
                    }
                    currentNode = nextNode;
                }
            }
            if (lastAccept) {
                addHitToMap(new Hit<>(hitEndPos, new String(text, hitStartPos,
                        hitEndPos - hitStartPos), hitValue), hitsEndPosMap);
            }
            return hitsEndPosMap.values();
        }

        @Override
        protected final List<Hit<V>> hitsResultHandle(char[] text, int startPos, int endPos, Collection<Hit<V>> collection) {
            //不做排序, 匹配的时候已经做过了
            return new ArrayList<>(collection);
        }
    }

}
