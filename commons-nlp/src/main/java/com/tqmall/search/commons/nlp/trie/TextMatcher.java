package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.nlp.Hit;
import com.tqmall.search.commons.nlp.Hits;

import java.util.*;

/**
 * Created by xing on 16/2/20.
 * 字符串匹配器, 将算法抽取出来, 与数据隔离
 */
public abstract class TextMatcher<V> {

    protected final Node<V> root;

    protected TextMatcher(Node<V> root) {
        this.root = root;
    }

    /**
     * 具体匹配实现
     *
     * @param charArray 待匹配的字符数组
     * @return 匹配结果, 如果返回的list认为错误,整个文本处理结果返回null
     */
    protected abstract Collection<Hit<V>> match(char[] charArray);

    /**
     * 匹配结果处理, 返回{@link Hits}对象
     */
    protected Hits<V> hitsResultHandle(char[] charArray, Collection<Hit<V>> collection) {
        Hits<V> hits = new Hits<>();
        if (!collection.isEmpty()) {
            List<Hit<V>> list;
            if (collection instanceof List) {
                list = (List<Hit<V>>) collection;
            } else {
                list = new ArrayList<>(collection);
            }
            Collections.sort(list);
            hits.addHits(collection);
        }
        Hits.initUnknownCharacters(hits, charArray);
        return hits;
    }

    /**
     * 文本匹配
     */
    public Hits<V> textMatch(String text) {
        char[] charArray = BinaryTrie.argCheck(text);
        if (charArray == null) return null;
        Collection<Hit<V>> list = match(charArray);
        if (list == null) return null;
        return hitsResultHandle(charArray, list);
    }

    public static <V> TextMatcher<V> minTextMatcher(Node<V> root) {
        return new MinBackTextMatcher<>(root);
    }

    public static <V> TextMatcher<V> maxTextMatcher(Node<V> root) {
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
        protected Collection<Hit<V>> match(char[] charArray) {
            List<Hit<V>> hits = new ArrayList<>();
            Node<V> currentNode = root;
            int i = charArray.length - 1, startPos = charArray.length, lastPos = charArray.length;
            while (i >= 0) {
                Node<V> nextNode = i < lastPos ? currentNode.getChild(charArray[i]) : null;
                if (nextNode == null || nextNode.getStatus() == Node.Status.DELETE) {
                    //没有匹配到, 向前移
                    if (currentNode == root) {
                        i--;
                    } else {
                        i = startPos - 1;
                        currentNode = root;
                    }
                } else {
                    //startPos只会不断减小
                    if (i < startPos) startPos = i;
                    i++;
                    if (nextNode.accept()) {
                        //匹配到一个词了~~~
                        hits.add(new Hit<>(i, new String(charArray, startPos, i - startPos), nextNode.getValue()));
                        i = startPos - 1;
                        lastPos = startPos;
                        currentNode = root;
                    } else {
                        currentNode = nextNode;
                    }
                }
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
        protected Collection<Hit<V>> match(char[] charArray) {
            Node<V> currentNode = root;
            int i = charArray.length - 1, startPos = charArray.length, hitEndPos = charArray.length;
            boolean lastAccept = false;
            V hitValue = null;
            Map<Integer, Hit<V>> hitsEndPosMap = new TreeMap<>();
            while (i >= 0) {
                Node<V> nextNode = i < charArray.length ? currentNode.getChild(charArray[i]) : null;
                if (nextNode == null || nextNode.getStatus() == Node.Status.DELETE) {
                    //没有匹配到, 向前移
                    if (currentNode == root) {
                        i--;
                    } else {
                        if (lastAccept) {
                            //匹配到一个词了~~~
                            addHitToMap(new Hit<>(hitEndPos, new String(charArray, startPos,
                                    hitEndPos - startPos), hitValue), hitsEndPosMap);
                            lastAccept = false;
                        }
                        i = startPos - 1;
                        currentNode = root;
                    }
                } else {
                    //startPos只会不断减小
                    if (i < startPos) startPos = i;
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
                addHitToMap(new Hit<>(hitEndPos, new String(charArray, startPos,
                        hitEndPos - startPos), hitValue), hitsEndPosMap);
            }
            return hitsEndPosMap.values();
        }

        @Override
        protected Hits<V> hitsResultHandle(char[] charArray, Collection<Hit<V>> collection) {
            Hits<V> hits = new Hits<>();
            //不做排序, 匹配的时候已经做过了
            hits.addHits(collection);
            Hits.initUnknownCharacters(hits, charArray);
            return hits;
        }
    }

}
