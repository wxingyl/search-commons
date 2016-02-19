package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.nlp.Hit;
import com.tqmall.search.commons.nlp.Hits;
import com.tqmall.search.commons.nlp.MatchResultHandle;

import java.util.Collections;

/**
 * Created by xing on 16/2/2.
 * 实现了模式匹配的二分查找前缀树,默认方法是最大匹配, 当然可以最小匹配{@link #textMinMatch(String)}
 * 如果需要最大,最小匹配都要, 那就用{@link AcTrie}吧
 *
 * @see AcTrie
 * @see AcBinaryTrie
 */
public class BinaryMatchTrie<V> extends BinaryTrie<V> {

    public BinaryMatchTrie(TrieNodeFactory<V> nodeFactory) {
        super(nodeFactory);
    }

    public Hits<V> textMaxMatch(String text) {
        return textMatch(text, new MatchProcess(false));
    }

    public Hits<V> textMinMatch(String text) {
        return textBackMatch(text);
    }

    private Hits<V> textMatch(String text, MatchProcess process) {
        char[] charArray = argCheck(text);
        if (charArray == null) return null;
        process.resultHandle = new MatchResultHandle<>(text);
        process.currentNode = root;
        try {
            int cursor = 0;
            while (cursor < charArray.length) {
                cursor = process.textMatchHandle(cursor, charArray[cursor]);
            }
            process.onFinish();
            Hits<V> hits = process.resultHandle.getHits();
            Hits.initUnknownCharacters(hits, charArray);
            return hits;
        } finally {
            process.resultHandle = null;
            process.currentNode = null;
        }
    }


    /**
     * 通过反向匹配
     *
     * @param text 待匹配输入文本
     * @return 返回结果根据匹配到的endPos, startPos正序排序
     */
    private Hits<V> textBackMatch(String text) {
        char[] charArray = argCheck(text);
        if (charArray == null) return null;
        Node<V> currentNode = root;
        Hits<V> hits = new Hits<>();
        int i = charArray.length - 1, startPos = charArray.length, lastPos = charArray.length;
        while (i >= 0) {
            Node<V> nextNode = i < lastPos ? currentNode.getChild(charArray[i]) : null;
            if (nextNode == null) {
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
                    hits.addHit(new Hit<>(i, text.substring(startPos, i), nextNode.getValue()));
                    i = startPos - 1;
                    lastPos = startPos;
                    currentNode = root;
                } else {
                    currentNode = nextNode;
                }
            }
        }
        if (!hits.getHits().isEmpty()) {
            Collections.sort(hits.getHits());
        }
        Hits.initUnknownCharacters(hits, charArray);
        return hits;
    }

    /**
     * 模式匹配类, 讲算法抽取出来, 与数据隔离
     */
    class MatchProcess {

        private final boolean isMin;

        /**
         * 执行前需要初始化
         */
        MatchResultHandle<V> resultHandle;

        /**
         * 执行前需要初始化
         */
        private Node<V> currentNode;

        private int curStartCursor = -1, curStopCursor = 0;
        /**
         * 最后是否有匹配, 最大匹配的时候需要
         */
        private boolean lastAccept = false;

        private V lastValue = null;

        /**
         * @param isMin 是否为最小匹配
         */
        public MatchProcess(boolean isMin) {
            this.isMin = isMin;
        }

        /**
         * @param index 当前处理字符串的地址
         * @param ch    处理的字符
         * @return 接下来需要处理字符的index
         */
        final int textMatchHandle(int index, char ch) {
            Node<V> nextNode = currentNode.getChild(ch);
            if (nextNode == null || nextNode.getStatus() == Node.Status.DELETE) {
                //没有匹配上
                if (currentNode == root) {
                    index++;
                } else {
                    if (lastAccept) {
                        //只有最大匹配才会到这儿, 讲原先匹配到词添加结果
                        index = curStopCursor;
                        onHit();
                    } else {
                        index = curStartCursor + 1;
                        curStartCursor = -1;
                        currentNode = root;
                    }
                }
            } else {
                if (curStartCursor == -1) {
                    curStartCursor = index;
                }
                index++;
                currentNode = nextNode;
                if (nextNode.accept()) {
                    lastAccept = true;
                    curStopCursor = index;
                    lastValue = nextNode.getValue();
                    if (isMin) {
                        onHit();
                    }
                }
            }
            return index;
        }

        final void onFinish() {
            if (isMin || !lastAccept) return;
            onHit();
        }

        private void onHit() {
            resultHandle.onHit(curStartCursor, curStopCursor, lastValue);
            currentNode = root;
            curStartCursor = -1;
            lastAccept = false;
            curStopCursor = 0;
        }

    }

}
