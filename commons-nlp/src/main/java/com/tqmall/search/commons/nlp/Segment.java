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
 * 分词, 通过{@link AcStrBinaryTrie}实现
 * 该类建议最好单例
 */
public class Segment {

    private static final Logger log = LoggerFactory.getLogger(Segment.class);

    private final AcStrBinaryTrie acBinaryTrie;

    private final Set<String> stopWords;

    private BinaryMatchTrie<Void> binaryMatchTrie;

    /**
     * 使用默认的{@link AcNormalNode#defaultCjkAcTrieNodeFactory()} 也就是词的开通只支持汉字
     */
    public Segment(InputStream lexicon) {
        this(AcNormalNode.<Void>defaultCjkAcTrieNodeFactory(), lexicon);
    }

    /**
     * 读取词库文件, 如果存在异常则抛出{@link LoadLexiconException}
     *
     * @param nodeFactory 具体初始化节点的Factory
     * @param lexicon     词库输入流
     * @see LoadLexiconException
     */
    public Segment(AcTrieNodeFactory<Void> nodeFactory, InputStream lexicon) {
        final AcStrBinaryTrie.Builder builder = AcStrBinaryTrie.build();
        builder.nodeFactory(nodeFactory);
        long startTime = System.currentTimeMillis();
        log.info("开始初始化词库: " + lexicon);
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
        log.info("加载词库: " + lexicon + "完成, 共耗时: " + (System.currentTimeMillis() - startTime) + "ms");
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
     * 新加指定停止词, 这儿修改不做多线程同步, 没加该停止词之前也在分词, 一样的~~~
     *
     * @return 添加是否成功
     */
    public boolean addStopWord(String word) {
        return stopWords.add(word);
    }

    /**
     * 删除停止词, 这儿修改不做多线程同步, 没加该停止词之前也在分词, 一样的~~~
     *
     * @return 删除是否成功
     */
    public boolean removeStopWord(String word) {
        return stopWords.remove(word);
    }

    /**
     * 索引分词, 尽可能的返回所有分词结果
     *
     * @param text 待分词文本
     * @return 分词结果
     */
    public List<Hit<Void>> fullSegment(char[] text) {
        Hits<Void> hits = acBinaryTrie.textMatch(text);
        return hitsFilter(hits);
    }

    /**
     * 最大分词匹配
     *
     * @param text 待输入文本
     * @return 最大分词结果
     */
    public List<Hit<Void>> maxSegment(char[] text) {
        Hits<Void> hits = binaryMatchTrie.textMaxMatch(text);
        return hitsFilter(hits);
    }

    /**
     * 最小分词匹配
     *
     * @param text 待输入文本
     * @return 最小分词结果
     */
    public List<Hit<Void>> minSegment(char[] text) {
        Hits<Void> hits = binaryMatchTrie.textMinMatch(text);
        return hitsFilter(hits);
    }

    private List<Hit<Void>> hitsFilter(Hits<Void> hits) {
        if (hits == null) return null;
        List<Hit<Void>> segmentList = new ArrayList<>();
        for (Hit<Void> h : hits) {
            if (stopWords.contains(h.getMatchKey())) continue;
            segmentList.add(h);
        }
        if (hits.getUnknownCharacters() != null) {
            for (MatchCharacter m : hits.getUnknownCharacters()) {
                String str = String.valueOf(m.getCharacter());
                if (stopWords.contains(str)) continue;
                segmentList.add(new Hit<Void>(m.getSrcPos(), str, null));
            }
        }
        return segmentList;
    }
}
