package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.nlp.trie.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by xing on 16/2/8.
 * 分词, 通过{@link AcStrBinaryTrie}实现
 */
final class Segment {

    private final AcStrBinaryTrie acBinaryTrie;

    private final Set<String> stopWords;

    private BinaryMatchTrie<Void> binaryMatchTrie;

    Segment() {
        final AcStrBinaryTrie.Builder builder = AcStrBinaryTrie.build();
        builder.nodeFactory(AcNormalNode.<Void>defaultCjkAcTrieNodeFactory());
        NlpUtils.loadLexicon(NlpConst.SEGMENT_FILE_NAME, new NlpUtils.LineHandle() {
            @Override
            public boolean onHandle(String line) {
                builder.add(line);
                return true;
            }
        });
        acBinaryTrie = builder.create(new Function<AcTrieNodeFactory<Void>, AbstractTrie<Void>>() {

            @Override
            public AbstractTrie<Void> apply(AcTrieNodeFactory<Void> acTrieNodeFactory) {
                binaryMatchTrie = new BinaryMatchTrie<>(acTrieNodeFactory);
                return binaryMatchTrie;
            }

        });
        stopWords = new HashSet<>();
        NlpUtils.loadLexicon(NlpConst.STOPWORD_FILE_NAME, new NlpUtils.LineHandle() {
            @Override
            public boolean onHandle(String line) {
                stopWords.add(line);
                return true;
            }
        });
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
