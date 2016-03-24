package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.analyzer.Stopword;
import com.tqmall.search.commons.analyzer.TokenType;

import java.util.Iterator;
import java.util.List;

/**
 * Created by xing on 16/3/17.
 * SegmentFilter相关工具类
 *
 * @author xing
 */
public final class SegmentFilters {

    private SegmentFilters() {
    }

    /**
     * 只处理待分词文本过滤
     *
     * @see TextFilter
     */
    public static SegmentFilter textFilter() {
        return TextFilter.INSTANCE;
    }

    /**
     * 实现停止词过滤, 同时支持{@link SegmentFilter#textFilter(char[], int, int)}
     *
     * @see Stopword
     * @see TextFilter
     * @see HitsFilter
     */
    public static SegmentFilter hitsFilter() {
        return HitsFilter.INSTANCE;
    }

    /**
     * 字符转换, 如果返回{@link Character#MIN_VALUE}, 则表示该字符没有未做转换
     *
     * @return 转换结果, 结果为{@link Character#MIN_VALUE}, 则表示该字符没有做任何转换
     */
    public static char charConvert(char c) {
        if (c >= 'A' && c <= 'Z') {
            //大写转小写
            return (char) (c + 32);
        } else if (c == '\u3000') {
            //全角空格处理
            return '\u0020';
        } else if (c > '\uFF00' && c < '\uFF5F') {
            //全角字符转半角
            return (char) (c - 65248);
        } else if (NlpUtils.isCjkChar(c)) {
            //中文繁体转简体
            return TraditionToSimple.instance().convert(c);
        } else {
            return Character.MIN_VALUE;
        }
    }

    /**
     * 只处理待分词文本过滤, 返回结果停止词就算了
     * 目前的处理有:
     * 1. 英文字母大写转小写
     * 2. 中文全角转半角
     * 3. 中文字符繁体转简体
     */
    static class TextFilter implements SegmentFilter {

        private static final TextFilter INSTANCE = new TextFilter();

        @Override
        public final void textFilter(char[] text, int off, int len) {
            final int endPos = off + len;
            NlpUtils.arrayIndexCheck(text, off, endPos);
            for (int i = off; i < endPos; i++) {
                char c = charConvert(text[i]);
                if (c != Character.MIN_VALUE) text[i] = c;
            }
        }

        @Override
        public void hitsFilter(char[] text, List<Hit<TokenType>> hits) {
            //do nothing
        }
    }

    /**
     * 实现停止词过滤, 同时支持{@link SegmentFilter#textFilter(char[], int, int)}
     *
     * @see Stopword
     * @see TextFilter
     */
    static class HitsFilter extends TextFilter {

        private static final HitsFilter INSTANCE = new HitsFilter();

        @Override
        public final void hitsFilter(char[] text, List<Hit<TokenType>> hits) {
            Iterator<Hit<TokenType>> it = hits.iterator();
            while (it.hasNext()) {
                Hit<TokenType> hit = it.next();
                if (Stopword.isStopword(text, hit.getStart(), hit.length())) {
                    //如果是停止词, 删除
                    it.remove();
                }
            }
        }
    }
}
