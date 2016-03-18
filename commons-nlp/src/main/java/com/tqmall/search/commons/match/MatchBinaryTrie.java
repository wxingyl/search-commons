package com.tqmall.search.commons.match;

import com.tqmall.search.commons.trie.BinaryTrie;
import com.tqmall.search.commons.trie.TrieNodeFactory;

import java.util.List;

/**
 * Created by xing on 16/2/2.
 * 字符匹配, 支持最大/最小, 正向/逆向匹配
 * 反向匹配, 建议通过{@link MatchBinaryReverseTrie}实现
 *
 * @see MatchBinaryReverseTrie
 */
public class MatchBinaryTrie<V> extends BinaryTrie<V> {

    private final TextMatcher<V> minTextMatcher;

    private final TextMatcher<V> maxTextMatcher;

    public MatchBinaryTrie(TrieNodeFactory<V> nodeFactory) {
        this(nodeFactory, false);
    }

    /**
     * @param nodeFactory 构造前缀树的nodeFactory
     * @param reverse     是否逆向匹配
     */
    public MatchBinaryTrie(TrieNodeFactory<V> nodeFactory, boolean reverse) {
        super(nodeFactory);
        minTextMatcher = TextMatcher.minMatcher(root, reverse);
        maxTextMatcher = TextMatcher.maxMatcher(root, reverse);
    }

    public List<Hit<V>> maxMatch(String text) {
        return maxTextMatcher.match(text.toCharArray(), 0, text.length());
    }

    public List<Hit<V>> maxMatch(char[] text, int startPos, int length) {
        return maxTextMatcher.match(text, startPos, length);
    }

    public List<Hit<V>> minMatch(String text) {
        return minTextMatcher.match(text.toCharArray(), 0, text.length());
    }

    public List<Hit<V>> minMatch(char[] text, int startPos, int length) {
        return minTextMatcher.match(text, startPos, length);
    }

}
