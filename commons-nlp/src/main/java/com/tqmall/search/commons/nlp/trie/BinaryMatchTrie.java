package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.nlp.Hit;

import java.util.List;

/**
 * Created by xing on 16/2/2.
 * 实现了模式匹配的二分查找前缀树,默认方法是最大匹配, 当然可以最小匹配{@link #textMinMatch(char[])}
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
        minTextMatcher = TextMatcher.minTextMatcher(root);
        maxTextMatcher = TextMatcher.maxTextMatcher(root);
    }

    public List<Hit<V>> textMaxMatch(char[] text) {
        return maxTextMatcher.textMatch(text);
    }

    public List<Hit<V>> textMaxMatch(char[] text, int startPos, int length) {
        return maxTextMatcher.textMatch(text, startPos, length);
    }

    public List<Hit<V>> textMinMatch(char[] text) {
        return minTextMatcher.textMatch(text);
    }

    public List<Hit<V>> textMinMatch(char[] text, int startPos, int length) {
        return minTextMatcher.textMatch(text, startPos, length);
    }

}
