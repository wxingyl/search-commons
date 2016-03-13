package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.match.TextMatch;

import java.util.List;
import java.util.Objects;

/**
 * Created by xing on 16/3/13.
 * 分词器, 其也是一个{@link TextMatch}
 *
 * @author xing
 */
public class Segment implements TextMatch<TokenType> {

    private final AsciiSegment asciiSegment;

    private final CjkSegment cjkSegment;

    public Segment(AsciiSegment asciiSegment, CjkSegment cjkSegment) {
        this.asciiSegment = asciiSegment;
        this.cjkSegment = cjkSegment;
    }

    @Override
    public List<Hit<TokenType>> match(char[] text) {
        Objects.requireNonNull(text);
        return match(text, 0, text.length);
    }

    @Override
    public List<Hit<TokenType>> match(char[] text, int startPos, int length) {
        return null;
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {

        private boolean parseDecimal;

        private boolean parseEnMix;

        private boolean enMixAppend;
        /**
         * 分文分词的分词类型
         */
        private SegmentType cjkSegmentType;

        public Builder parseDecimal(boolean parseDecimal) {
            this.parseDecimal = parseDecimal;
            return this;
        }

        /**
         * 识别EnMix词
         */
        private Builder enMixAppend(boolean enMixAppend) {
            this.parseEnMix = true;
            this.enMixAppend = enMixAppend;
            return this;
        }

        private Builder cjkSegmentType(SegmentType cjkSegmentType) {
            this.cjkSegmentType = cjkSegmentType;
            return this;
        }

        public Segment create(CjkLexicon cjkLexicon) {
            Objects.requireNonNull(cjkLexicon);
            return new Segment(new AsciiSegment(parseDecimal, parseEnMix, enMixAppend),
                    CjkSegment.createSegment(cjkLexicon, cjkSegmentType));
        }
    }
}
