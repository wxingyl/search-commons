package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.lang.LazyInit;
import com.tqmall.search.commons.lang.Supplier;
import com.tqmall.search.commons.utils.SearchStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xing on 16/1/26.
 * 繁体转简体, 按需加载词库
 * 目前我们只用到繁体转简体, 至于简体转繁体,暂时不care, 没有用到
 */
public final class TraditionToSimple {

    private static final Logger log = LoggerFactory.getLogger(TraditionToSimple.class);

    private static final LazyInit<TraditionToSimple> INSTANCE = new LazyInit<>(new Supplier<TraditionToSimple>() {
        @Override
        public TraditionToSimple get() {
            return new TraditionToSimple();
        }
    });

    /**
     * 单例, 通过该接口获取实例对象
     */
    public static TraditionToSimple instance() {
        return INSTANCE.getInstance();
    }

    /**
     * 字符数据,大小为CJK标准字符个数, 目前是
     */
    private final char[] chars;

    TraditionToSimple() {
        final int indexOffset = NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST;
        log.info("start loading TraditionToSimple lexicon file: " + NlpConst.F2J_FILE_NAME);
        //都是本地加载, 数据格式的校验就不要太严格了~~~
        chars = new char[NlpConst.CJK_UNIFIED_SIZE];
        NlpUtils.loadClassPathLexicon(TraditionToSimple.class, NlpConst.F2J_FILE_NAME, new Function<String, Boolean>() {
            @Override
            public Boolean apply(String line) {
                String[] array = SearchStringUtils.split(line, '=');
                if (array.length < 2) {
                    log.warn("load tradition to simple lexicon file, word: " + line + " is invalid format");
                } else {
                    chars[array[0].charAt(0) - indexOffset] = array[1].charAt(0);
                }
                return true;
            }
        });
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == 0) {
                chars[i] = (char) (indexOffset + i);
            }
        }
        log.info("load TraditionToSimple lexicon file: " + NlpConst.F2J_FILE_NAME + " finish");
    }

    /**
     * 是否位繁体字符
     * 该函数跟{@link #convert(char)}的性能一样一样的, 并没有
     *
     * @param ch 判断字符
     * @return true 为繁体
     */
    public final boolean isTraditional(char ch) {
        return convert(ch) != ch;
    }

    /**
     * 如果是CJK字符, 返回对应的转换字符, 如果不是则返回入参ch
     */
    public final char convert(char ch) {
        return ch < NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST || ch > NlpConst.CJK_UNIFIED_IDEOGRAPHS_LAST ?
                ch : chars[ch - NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST];
    }

    /**
     * 修改原字符数组转换繁体为简体
     */
    public final void convert(final char[] text, final int startPos, final int length) {
        final int endPos = startPos + length;
        NlpUtils.arrayIndexCheck(text, startPos, endPos);
        for (int i = startPos; i < endPos; i++) {
            text[i] = convert(text[i]);
        }
    }

    /**
     * 如果传入的字符串有繁体, 则转换成简体字符串
     * 如果没有繁体, 则不做装换, 原样返回
     * 很多情况下, 字符串里面没有繁体字符的
     */
    public final String convert(String str) {
        if (SearchStringUtils.isEmpty(str)) return str;
        int length = str.length();
        char[] array = null;
        for (int i = 0; i < length; i++) {
            char c = convert(str.charAt(i));
            if (c != str.charAt(i) && array == null) {
                array = str.toCharArray();
            }
            if (array != null) {
                array[i] = c;
            }
        }
        return array == null ? str : new String(array);
    }

}
