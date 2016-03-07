package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.nlp.Hit;

import java.util.List;

/**
 * Created by xing on 16/2/2.
 * 实现了模式匹配的二分查找前缀树,默认方法是最大匹配, 当然可以最小匹配{@link #minMatch(char[])}
 * 如果需要最大,最小匹配都要, 那就用{@link AcTrie}吧
 *
 * @see AcTrie
 * @see AcBinaryTrie
 */
public class BinaryMatchTrie<V> extends BinaryTrie<V> {

    private final TextMatcher<V> minTextMatcher;

    private final TextMatcher<V> maxTextMatcher;

    public BinaryMatchTrie(TrieNodeFactory<V> nodeFactory) {
        super(nodeFactory);
        minTextMatcher = TextMatcher.minMatcher(root);
        maxTextMatcher = TextMatcher.maxMatcher(root);
    }

    public List<Hit<V>> maxMatch(char[] text) {
        return maxTextMatcher.match(text);
    }

    public List<Hit<V>> maxMatch(char[] text, int startPos, int length) {
        return maxTextMatcher.match(text, startPos, length);
    }

    public List<Hit<V>> minMatch(char[] text) {
        return minTextMatcher.match(text);
    }

    public List<Hit<V>> minMatch(char[] text, int startPos, int length) {
        return minTextMatcher.match(text, startPos, length);
    }

}
