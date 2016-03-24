package com.tqmall.search.commons.match;

import com.tqmall.search.commons.nlp.NlpUtils;
import com.tqmall.search.commons.trie.BinaryTrie;
import com.tqmall.search.commons.trie.Node;
import com.tqmall.search.commons.trie.TrieNodeFactory;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.*;

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
    public Node<V> getNode(char[] key, int off, int len) {
        char[] array = Arrays.copyOfRange(key, off, off + len);
        return getNodeInner(array, 0, len);
    }

    @Override
    protected Node<V> getNodeInner(char[] key, int off, int len) {
        NlpUtils.reverseCharArray(key, off, len);
        return super.getNodeInner(key, off, len);
    }

    @Override
    protected boolean put(char[] key, V value) {
        NlpUtils.reverseCharArray(key);
        return super.put(key, value);
    }

    public List<Hit<V>> maxMatch(String text) {
        return doMatch(text.toCharArray(), 0, text.length(), true);
    }

    public List<Hit<V>> maxMatch(char[] text, int off, int len) {
        return doMatch(text, off, len, true);
    }

    public List<Hit<V>> minMatch(String text) {
        return doMatch(text.toCharArray(), 0, text.length(), false);
    }

    public List<Hit<V>> minMatch(char[] text, int off, int len) {
        return doMatch(text, off, len, false);
    }

    @Override
    public List<Map.Entry<String, V>> prefixSearch(String word) {
        List<Map.Entry<String, V>> result = super.prefixSearch(word);
        if (!CommonsUtils.isEmpty(result)) {
            ListIterator<Map.Entry<String, V>> it = result.listIterator();
            while (it.hasNext()) {
                Map.Entry<String, V> e = it.next();
                it.set(new AbstractMap.SimpleImmutableEntry<>(NlpUtils.reverseString(e.getKey()), e.getValue()));
            }
        }
        return result;
    }

    private List<Hit<V>> doMatch(char[] text, int off, int len, boolean maxMatch) {
        char[] array = Arrays.copyOfRange(text, off, off + len);
        NlpUtils.reverseCharArray(array);
        List<Hit<V>> hits = maxMatch ? maxTextMatcher.match(array, 0, len) : minTextMatcher.match(array, 0, len);
        if (!CommonsUtils.isEmpty(hits)) {
            int offsetIndex = off + len;
            for (Hit<V> h : hits) {
                h.changePosition(offsetIndex - h.getEnd(), offsetIndex - h.getStart());
            }
            Collections.reverse(hits);
        }
        return hits;
    }


}
