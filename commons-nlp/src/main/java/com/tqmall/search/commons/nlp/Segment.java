package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.nlp.trie.AcNormalNode;
import com.tqmall.search.commons.nlp.trie.AcStrBinaryTrie;

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
        acBinaryTrie = builder.create();
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
     * 最小分词, 按照词典中的词最小纬度分词
     *
     * @param text 待分词文本
     * @return 分词结果
     */
    public List<Hit<Void>> segment(String text) {
        Hits<Void> hits = acBinaryTrie.textMatch(text);
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
