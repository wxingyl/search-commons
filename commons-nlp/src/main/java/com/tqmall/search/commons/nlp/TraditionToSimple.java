package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.exception.LoadLexiconException;
import com.tqmall.search.commons.utils.SearchStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Created by xing on 16/1/26.
 * 繁体转简体, 按需加载词库
 * 目前我们只用到繁体转简体, 至于简体转繁体,暂时不care, 没有用到
 */
final class TraditionToSimple {

    private static final Logger log = LoggerFactory.getLogger(TraditionToSimple.class);

    private static final String F2J_FILE_NAME = "tradition-simple.txt";

    private static volatile TraditionToSimple INSTANCE;

    /**
     * 获取TraditionToSimple实例, 这儿需要保证TraditionToSimple为单例
     */
    public static TraditionToSimple getInstance() {
        if (INSTANCE == null) {
            synchronized (TraditionToSimple.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TraditionToSimple();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 字符数据,大小为CJK标准字符个数, 目前是
     */
    private final char[] chars;

    private TraditionToSimple() {
        chars = load();
    }

    /**
     * 都是本地加载, 数据格式的校验就不要太严格了~~~
     */
    private char[] load() {
        log.info("开始加载繁体转简体词库: " + F2J_FILE_NAME);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream('/' + F2J_FILE_NAME),
                StandardCharsets.UTF_8))) {
            String line;
            final int indexOffset = NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST;
            char[] localCharArray = new char[NlpConst.CJK_UNIFIED_SIZE];
            int loadTraditionCount = 0;
            while ((line = reader.readLine()) != null) {
                String[] array = SearchStringUtils.split(line, '=');
                if (array.length < 2) {
                    log.warn("加载繁体转简体词库, 词" + line + "格式存在异常");
                } else {
                    localCharArray[array[0].charAt(0) - indexOffset] = array[1].charAt(0);
                    loadTraditionCount++;
                }
            }
            for (int i = 0; i < localCharArray.length; i++) {
                if (localCharArray[i] == 0) {
                    localCharArray[i] = (char) (indexOffset + i);
                }
            }
            log.info("加载繁体转简体词库: " + F2J_FILE_NAME + " 完成, 共加载了" + loadTraditionCount + "个繁体对应简体词库");
            return localCharArray;
        } catch (IOException e) {
            log.error("加载繁体对应简体字典: " + F2J_FILE_NAME + "时存在异常", e);
            throw new LoadLexiconException(F2J_FILE_NAME, e);
        }
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
     * 如果传入的字符串有繁体, 则转换成简体字符串
     * 如果没有繁体, 则不做装换, 原样返回
     * 很对情况下, 砸门的字符串里面没有繁体字符的
     */
    public final String convert(String str) {
        if (SearchStringUtils.isEmpty(str)) return str;
        int length = str.length();
        char[] array = null;
        for (int i = 0; i < length; i++) {
            char ch = convert(str.charAt(i));
            if (ch != str.charAt(i) && array == null) {
                array = new char[length];
                for (int j = 0; j < i; j++) {
                    array[j] = str.charAt(j);
                }
            }
            if (array != null) {
                array[i] = ch;
            }
        }
        return array == null ? str : new String(array);
    }

}
