package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.exception.LoadLexiconException;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.nlp.trie.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xing on 16/2/8.
 * 分词, 通过{@link AcStrBinaryTrie}实现
 * 该类应该是单例的
 */
public class Segment {

    private static final Logger log = LoggerFactory.getLogger(Segment.class);

    private final AcStrBinaryTrie acBinaryTrie;

    /**
     * 给下面的{@link #stopWords}使用, 实现ConcurrentSet的功能
     */
    private final Object PRESENT = new Object();

    private final ConcurrentMap<String, Object> stopWords;

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
            NlpUtils.loadLexicon(lexicon, new NlpUtils.LineHandle() {
                @Override
                public boolean onHandle(String line) {
                    builder.add(line);
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
        stopWords = new ConcurrentHashMap<>();
        NlpUtils.loadLexicon(NlpConst.STOPWORD_FILE_NAME, new NlpUtils.LineHandle() {
            @Override
            public boolean onHandle(String line) {
                stopWords.put(line, PRESENT);
                return true;
            }
        });
    }

    /**
     * 新加指定停止词
     *
     * @return 添加是否成功
     */
    public boolean addStopWord(String word) {
        return stopWords.put(word, PRESENT) == null;
    }

    /**
     * 删除停止词
     *
     * @return 删除是否成功
     */
    public boolean removeStopWord(String word) {
        return stopWords.remove(word) == PRESENT;
    }

    /**
     * 索引分词, 尽可能的返回所有分词结果
     *
     * @param text 待分词文本
     * @return 分词结果
     */
    public List<Hit<Void>> fullSegment(String text) {
        Hits<Void> hits = acBinaryTrie.textMatch(text);
        return hitsFilter(hits);
    }

    /**
     * 最大分词匹配
     *
     * @param text 待输入文本
     * @return 最大分词结果
     */
    public List<Hit<Void>> maxSegment(String text) {
        Hits<Void> hits = binaryMatchTrie.textMaxMatch(text);
        return hitsFilter(hits);
    }

    /**
     * 最小分词匹配
     *
     * @param text 待输入文本
     * @return 最小分词结果
     */
    public List<Hit<Void>> minSegment(String text) {
        Hits<Void> hits = binaryMatchTrie.textMinMatch(text);
        return hitsFilter(hits);
    }

    private List<Hit<Void>> hitsFilter(Hits<Void> hits) {
        List<Hit<Void>> segmentList = new ArrayList<>();
        for (Hit<Void> h : hits) {
            if (stopWords.containsKey(h.getMatchKey())) continue;
            segmentList.add(h);
        }
        if (hits.getUnknownCharacters() != null) {
            for (MatchCharacter m : hits.getUnknownCharacters()) {
                String str = String.valueOf(m.getCharacter());
                if (stopWords.containsKey(str)) continue;
                segmentList.add(new Hit<Void>(m.getSrcPos(), str, null));
            }
        }
        return segmentList;
    }
}
