package com.tqmall.search.commons.nlp.trie;

import java.util.ArrayList;
import java.util.List;

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

    public List<Hit<V>> textMaxMatch(String text) {
        return textMatch(text, false);
    }

    public List<Hit<V>> textMinMatch(String text) {
        return textMatch(text, true);
    }

    private List<Hit<V>> textMatch(String text, boolean isMin) {
        char[] charArray = argCheck(text);
        if (charArray == null) return null;
        List<Hit<V>> resultList = new ArrayList<>();
        Node<V> currentNode = root;
        int i = 0, nextStartCursor = 0;
        boolean lastAccept = false;
        V lastValue = null;
        StringBuilder sb = new StringBuilder();
        while (i < charArray.length) {
            char ch = charArray[i];
            Node<V> nextNode = currentNode.getChild(ch);
            if (nextNode == null || nextNode.getStatus() == Node.Status.DELETE) {
                if (currentNode == root) {
                    i++;
                } else {
                    currentNode = root;
                    i = nextStartCursor;
                    if (lastAccept) {
                        resultList.add(new Hit<>(nextStartCursor, sb.toString(), lastValue));
                        lastAccept = false;
                    }
                    sb.setLength(0);
                    nextStartCursor = 0;
                }
            } else {
                i++;
                sb.append(ch);
                if (nextStartCursor == 0) {
                    nextStartCursor = i;
                }
                currentNode = nextNode;
                if (nextNode.accept()) {
                    lastAccept = true;
                    nextStartCursor = i;
                    lastValue = nextNode.getValue();
                    if (isMin) {
                        resultList.add(new Hit<>(nextStartCursor, sb.toString(), lastValue));
                        currentNode = root;
                        sb.setLength(0);
                        lastAccept = false;
                        nextStartCursor = 0;
                    }
                }
            }
        }
        if (lastAccept) {
            resultList.add(new Hit<>(nextStartCursor, sb.toString(), lastValue));
        }
        return resultList;
    }

}
