package com.tqmall.search.commons.analyzer;

import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.lang.LazyInit;
import com.tqmall.search.commons.lang.Supplier;
import com.tqmall.search.commons.nlp.NlpConst;
import com.tqmall.search.commons.nlp.NlpUtils;
import com.tqmall.search.commons.trie.BinaryTrie;
import com.tqmall.search.commons.trie.Node;
import com.tqmall.search.commons.trie.NodeChildHandle;
import com.tqmall.search.commons.trie.RootNodeType;
import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.*;

/**
 * Created by xing on 16/3/8.
 * 停止词, 不区分大小写
 *
 * @author xing
 */
public class Stopword {

    private static final LazyInit<Stopword> INSTANCE = new LazyInit<>(new Supplier<Stopword>() {
        @Override
        public Stopword get() {
            return new Stopword();
        }
    });

    /**
     * 单例, 通过该接口获取实例对象
     */
    public static Stopword instance() {
        return INSTANCE.getInstance();
    }

    /**
     * 判断是否为停止词
     */
    public static boolean isStopword(char[] text, int off, int len) {
        Node node = INSTANCE.getInstance().stopWords.getNode(text, off, len);
        return node != null && node.accept();
    }

    private final BinaryTrie<Void> stopWords;

    Stopword() {
        stopWords = new BinaryTrie<>(RootNodeType.NORMAL.<Void>defaultTrie());
        NlpUtils.loadLexicon(NlpConst.STOPWORD_FILE_NAME, new Function<String, Boolean>() {
            @Override
            public Boolean apply(String line) {
                stopWords.put(line, null);
                return true;
            }
        });
    }

    /**
     * 添加停止词
     *
     * @return 是否添加完成
     */
    public boolean addStopword(String word) {
        word = SearchStringUtils.filterString(word);
        return word != null && stopWords.put(word.toLowerCase(), null);
    }

    /**
     * 删除停止词
     *
     * @return 是否删除完成
     */
    public boolean removeStopword(String word) {
        word = SearchStringUtils.filterString(word);
        return word != null && stopWords.remove(word);
    }

    /**
     * 获取所有的停止词
     */
    public Set<String> allStopwords() {
        Node<Void> root = stopWords.getRoot();
        final List<Character> list = new ArrayList<>();
        root.childHandle(new NodeChildHandle<Void>() {
            @Override
            public boolean onHandle(Node<Void> child) {
                list.add(child.getChar());
                return true;
            }
        });
        Set<String> allStopWords = new HashSet<>();
        for (Character c : list) {
            List<Map.Entry<String, Void>> ret = stopWords.prefixSearch(c.toString());
            if (ret != null) {
                for (Map.Entry<String, Void> e : ret) {
                    allStopWords.add(e.getKey());
                }
            }
        }
        return allStopWords;
    }

}
