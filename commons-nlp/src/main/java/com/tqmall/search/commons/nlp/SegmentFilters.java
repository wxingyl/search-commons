package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.match.Hit;

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
     * 只处理待分词文本过滤, 返回结果停止词就算了
     * 目前的处理有:
     * 1. 英文字母大写转小写
     * 2. 中文全角转半角
     * 3. 中文字符繁体转简体
     */
    static class TextFilter implements SegmentFilter {

        private static final TextFilter INSTANCE = new TextFilter();

        @Override
        public final void textFilter(char[] text, int startPos, int length) {
            final int endPos = startPos + length;
            NlpUtils.arrayIndexCheck(text, startPos, endPos);
            for (int i = startPos; i < endPos; i++) {
                char c = text[i];
                if (c >= 'A' && c <= 'Z') {
                    //大写转小写
                    c += 32;
                } else if (c == '\u3000') {
                    //全角空格处理
                    c = '\u0020';
                } else if (c > '\uFF00' && c < '\uFF5F') {
                    //全角字符转半角
                    c -= 65248;
                } else if (NlpUtils.isCjkChar(c)) {
                    //中文繁体转简体
                    c = TraditionToSimple.instance().convert(c);
                } else continue;
                text[i] = c;
            }
        }

        @Override
        public void hitsFilter(List<Hit<TokenType>> hits) {
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
        public final void hitsFilter(List<Hit<TokenType>> hits) {
            Iterator<Hit<TokenType>> it = hits.iterator();
            while (it.hasNext()) {
                if (Stopword.isStopword(it.next().getKey())) {
                    //如果是停止词, 删除
                    it.remove();
                }
            }
        }
    }
}
