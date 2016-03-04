package com.tqmall.search.commons.nlp;

import com.sun.org.apache.xalan.internal.xsltc.dom.BitArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xing on 16/2/10.
 * 匹配结果集
 */
public class Hits<V> implements Iterable<Hit<V>> {

    private List<Hit<V>> hits = new ArrayList<>();

    /**
     * 注意: 这儿{@link MatchCharacter#srcPos} 是原始文本中的坐标pos, 并不是开区间结束位置
     */
    private List<MatchCharacter> unknownCharacters;

    public void addHit(Hit<V> hit) {
        hits.add(hit);
    }

    public void addHits(Collection<? extends Hit<V>> hit) {
        hits.addAll(hit);
    }

    public void addUnknownCharacter(char c, int pos) {
        if (unknownCharacters == null) {
            unknownCharacters = new ArrayList<>();
        }
        unknownCharacters.add(new MatchCharacter(c, pos));
    }

    public List<Hit<V>> getHits() {
        return hits;
    }

    public List<MatchCharacter> getUnknownCharacters() {
        return unknownCharacters;
    }

    @Override
    public Iterator<Hit<V>> iterator() {
        return hits.iterator();
    }

    @Override
    public String toString() {
        return hits.toString() + (unknownCharacters == null ? "" : ", unknown: " + unknownCharacters);
    }

    /**
     * 初始化{@link Hits#unknownCharacters}
     *
     * @param charArray 解析text的原数组
     */
    public static <V> void initUnknownCharacters(Hits<V> hits, char[] charArray) {
        BitArray bitArray = new BitArray(charArray.length);
        for (Hit h : hits) {
            for (int i = h.getStartPos(); i < h.getEndPos(); i++) {
                bitArray.setBit(i);
            }
        }
        for (int i = 0; i < charArray.length; i++) {
            if (!bitArray.getBit(i)) {
                hits.addUnknownCharacter(charArray[i], i);
            }
        }
    }
}
