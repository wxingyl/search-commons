package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.nlp.Hits;
import com.tqmall.search.commons.nlp.MatchResultHandle;

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
        return textMatch(text, new MatchProcess(true));
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
                if (currentNode == root) {
                    //没有匹配上
                    index++;
                } else {
                    if (lastAccept) {
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
