package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.match.TextMatch;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by xing on 16/3/16.
 * 字母,数字最大分词, 相连的数字,字母都整到一个词里面
 *
 * @author xing
 */
public class MaxAsciiSegment implements TextMatch<TokenType> {

    public static final MaxAsciiSegment INSTANCE = new MaxAsciiSegment();

    MaxAsciiSegment() {
    }

    @Override
    public List<Hit<TokenType>> match(char[] text) {
        return this.match(text, 0, text.length);
    }

    private boolean usefulChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
    }

    private boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }

    @Override
    public List<Hit<TokenType>> match(char[] text, int startPos, int length) {
        final int endPos = startPos + length;
        NlpUtils.arrayIndexCheck(text, startPos, endPos);
        if (length == 0) return null;
        List<Hit<TokenType>> hits = new LinkedList<>();
        int matchStart = -1;
        for (int i = startPos; i < endPos; i++) {
            char c = text[i];
            if (usefulChar(c) || (c == '.' && i > startPos && (i + 1) < endPos
                    && isNumber(text[i - 1]) && isNumber(text[i + 1]))) {
                if (matchStart == -1) matchStart = i;
            } else if (matchStart != -1) {
                hits.add(new Hit<>(text, matchStart, i, TokenType.EN_MIX));
                matchStart = -1;
            }
        }
        if (matchStart != -1) {
            hits.add(new Hit<>(text, matchStart, endPos, TokenType.EN_MIX));
        }
        return hits;
    }

}
