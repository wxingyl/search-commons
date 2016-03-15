package com.tqmall.search.commons.match;

import com.tqmall.search.commons.nlp.NlpUtils;
import com.tqmall.search.commons.trie.BinaryTrie;
import com.tqmall.search.commons.trie.Node;
import com.tqmall.search.commons.trie.TrieNodeFactory;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/3/15.
 * 逆向二分前缀树
 * 对于{@link com.tqmall.search.commons.trie.Trie#prefixSearch(String)}, 参数依然是正向的, 只不过从后向前匹配, 比如对于词:
 * <p/>
 * 北京大学地铁站
 * 清华地铁站
 * 西湖地铁站
 * 杭州西湖
 * <p/>
 * 参数为"地铁站", 返回结果为"北京大学地铁站", "清华地铁站", "西湖地铁站"
 *
 * @author xing
 * @see MatchBinaryTrie
 */
public class MatchBinaryReverseTrie<V> extends BinaryTrie<V> {

    private final TextMatcher<V> minTextMatcher;

    private final TextMatcher<V> maxTextMatcher;

    /**
     * @param nodeFactory 构造前缀树的nodeFactory
     */
    public MatchBinaryReverseTrie(TrieNodeFactory<V> nodeFactory) {
        super(nodeFactory);
        minTextMatcher = TextMatcher.minMatcher(root, false);
        maxTextMatcher = TextMatcher.maxMatcher(root, false);
    }

    @Override
    protected Node<V> getNode(char[] array) {
        NlpUtils.reverseCharArray(array);
        return super.getNode(array);
    }

    @Override
    protected boolean put(char[] key, V value) {
        NlpUtils.reverseCharArray(key);
        return super.put(key, value);
    }

    public List<Hit<V>> maxMatch(String text) {
        return doMatch(text.toCharArray(), 0, text.length(), true);
    }

    public List<Hit<V>> maxMatch(char[] text, int startPos, int length) {
        return doMatch(text, startPos, length, true);
    }

    public List<Hit<V>> minMatch(String text) {
        return doMatch(text.toCharArray(), 0, text.length(), false);
    }

    public List<Hit<V>> minMatch(char[] text, int startPos, int length) {
        return doMatch(text, startPos, length, false);
    }


    private List<Hit<V>> doMatch(char[] text, int startPos, int length, boolean maxMatch) {
        char[] array = Arrays.copyOfRange(text, startPos, startPos + length);
        NlpUtils.reverseCharArray(array);
        List<Hit<V>> hits = maxMatch ? maxTextMatcher.match(array, 0, length) : minTextMatcher.match(array, 0, length);
        if (!CommonsUtils.isEmpty(hits)) {
            int offsetIndex = startPos + length;
            for (Hit<V> h : hits) {
                h.changeKey(offsetIndex - h.getEndPos(), NlpUtils.reverseString(h.getKey()));
            }
            Collections.reverse(hits);
        }
        return hits;
    }
}
