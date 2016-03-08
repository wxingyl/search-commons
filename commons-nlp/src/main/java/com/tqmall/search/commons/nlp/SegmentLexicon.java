package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.exception.LoadLexiconException;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.nlp.trie.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by xing on 16/2/8.
 * 分词词库, 包括汉语词库以及停止词, 通过{@link AcStrBinaryTrie}实现
 * 该类建议最好单例
 */
public class SegmentLexicon {

    private static final Logger log = LoggerFactory.getLogger(SegmentLexicon.class);

    private final AcStrBinaryTrie acBinaryTrie;

    private final Set<String> stopWords;

    private BinaryMatchTrie<Void> binaryMatchTrie;

    /**
     * 使用默认的{@link AcNormalNode#defaultCjkAcTrieNodeFactory()} 也就是词的开通只支持汉字
     */
    public SegmentLexicon(InputStream lexicon) {
        this(AcNormalNode.<Void>defaultCjkAcTrieNodeFactory(), lexicon);
    }

    /**
     * 读取词库文件, 如果存在异常则抛出{@link LoadLexiconException}
     *
     * @param nodeFactory 具体初始化节点的Factory
     * @param lexicon     词库输入流
     * @see LoadLexiconException
     */
    public SegmentLexicon(AcTrieNodeFactory<Void> nodeFactory, InputStream lexicon) {
        final AcStrBinaryTrie.Builder builder = AcStrBinaryTrie.build();
        builder.nodeFactory(nodeFactory);
        long startTime = System.currentTimeMillis();
        log.info("start loading cjk lexicon: " + lexicon);
        try {
            NlpUtils.loadLexicon(lexicon, new Function<String, Boolean>() {
                @Override
                public Boolean apply(String s) {
                    builder.add(s);
                    return true;
                }
            }, true);
        } catch (IOException e) {
            log.error("读取词库: " + lexicon + " 存在IOException", e);
            throw new LoadLexiconException("初始化分词Segment: " + lexicon + ", 读取词库异常", e);
        }
        acBinaryTrie = builder.create(new Function<AcTrieNodeFactory<Void>, AbstractTrie<Void>>() {

            @Override
            public AbstractTrie<Void> apply(AcTrieNodeFactory<Void> acTrieNodeFactory) {
                binaryMatchTrie = new BinaryMatchTrie<>(acTrieNodeFactory);
                return binaryMatchTrie;
            }

        });
        log.info("load cjk lexicon: " + lexicon + " finish, total cost: " + (System.currentTimeMillis() - startTime) + "ms");
        stopWords = new HashSet<>();
        NlpUtils.loadLexicon(NlpConst.STOPWORD_FILE_NAME, new Function<String, Boolean>() {
            @Override
            public Boolean apply(String line) {
                stopWords.add(line);
                return true;
            }
        });
    }

    /**
     * 获取停止词列表, 返回的Set可以被修改
     *
     * @return 可以修改的停止词列表
     */
    public Set<String> getStopWords() {
        return stopWords;
    }

    /**
     * 索引分词, 尽可能的返回所有分词结果
     *
     * @param text 待分词文本
     * @return 分词结果
     */
    public List<Hit<Void>> fullMatch(char[] text) {
        return hitsFilter(acBinaryTrie.match(text));
    }

    /**
     * 最大分词匹配
     *
     * @param text 待输入文本
     * @return 最大分词结果
     */
    public List<Hit<Void>> maxMatch(char[] text) {
        return hitsFilter(binaryMatchTrie.maxMatch(text));
    }

    /**
     * 最小分词匹配
     *
     * @param text 待输入文本
     * @return 最小分词结果
     */
    public List<Hit<Void>> minMatch(char[] text) {
        return hitsFilter(binaryMatchTrie.minMatch(text));
    }

    private List<Hit<Void>> hitsFilter(List<Hit<Void>> hits) {
        if (hits == null) return null;
        List<Hit<Void>> segmentList = new ArrayList<>();
        for (Hit<Void> h : hits) {
            if (stopWords.contains(h.getMatchKey())) continue;
            segmentList.add(h);
        }
        return segmentList;
    }
}
