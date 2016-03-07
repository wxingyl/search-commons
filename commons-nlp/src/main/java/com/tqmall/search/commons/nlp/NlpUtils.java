package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.exception.LoadLexiconException;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.lang.LazyInit;
import com.tqmall.search.commons.lang.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Created by xing on 16/1/26.
 * Nlp 操作Utils类
 */
public final class NlpUtils {

    private static final Logger log = LoggerFactory.getLogger(NlpUtils.class);

    /**
     * 繁体转简体实例
     */
    private static final LazyInit<TraditionToSimple> TRADITION_TO_SIMPLE = new LazyInit<>(new Supplier<TraditionToSimple>() {
        @Override
        public TraditionToSimple get() {
            return new TraditionToSimple();
        }
    });

    /**
     * 拼音转换实例
     */
    private static final LazyInit<PinyinConvert> PINYIN_CONVERT = new LazyInit<>(new Supplier<PinyinConvert>() {
        @Override
        public PinyinConvert get() {
            log.info("开始加载拼音词库");
            long startTime = System.currentTimeMillis();
            PinyinConvert convert = new PinyinConvert();
            log.info("加载拼音词库完成, 耗时: " + (System.currentTimeMillis() - startTime) + "ms");
            return convert;
        }
    });

    private NlpUtils() {
    }

    /**
     * 判断是否为cjk字符
     *
     * @return true为cjk字符
     */
    public static boolean isCjkChar(char ch) {
        return ch >= NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST && ch <= NlpConst.CJK_UNIFIED_IDEOGRAPHS_LAST;
    }

    /**
     * 全角字符转半角
     *
     * @return 如果是全角, 则返回对应字符, 如果不是全角则返回自身
     */
    public static char fullwidthConvert(char ch) {
        //空格特殊处理
        if (ch == '\u3000') {
            return '\u0020';
        } else if (ch > 65280 && ch < 65375) {
            return (char) (ch - 65248);
        } else {
            return ch;
        }
    }

    /**
     * 是否为繁体字符
     * 该函数跟{@link #cjkConvert(char)}的性能一样一样的, 并没有
     *
     * @param ch 判断字符
     * @return true 为繁体
     */
    public static boolean isTraditional(char ch) {
        return TRADITION_TO_SIMPLE.getInstance().isTraditional(ch);
    }

    /**
     * 如果是CJK字符, 返回对应的转换字符, 如果不是则返回入参ch
     */
    public static char cjkConvert(char ch) {
        return TRADITION_TO_SIMPLE.getInstance().convert(ch);
    }

    /**
     * 如果传入的字符串有繁体, 则转换成简体字符串
     * 如果没有繁体, 则不做装换, 原样返回
     * 很对情况下, 砸门的字符串里面没有繁体字符的
     */
    public static String traditionalConvert(String str) {
        return TRADITION_TO_SIMPLE.getInstance().convert(str);
    }

    /**
     * 单个cjk字符转化, 对于多音字, 只返回词库中的第一个
     */
    public static String pyConvert(char cjkChar) {
        return PINYIN_CONVERT.getInstance().convert(cjkChar);
    }

    /**
     * 字符串拼音转换
     *
     * @param word       需要转换的汉字
     * @param appendFlag 需要包含的字符: 数字,空格等字符标记位
     * @return 转换结果
     * @see NlpConst#APPEND_CHAR_WHITESPACE
     * @see NlpConst#APPEND_CHAR_LETTER
     * @see NlpConst#APPEND_CHAR_DIGIT
     */
    public static String pyConvert(char[] word, int appendFlag) {
        return PINYIN_CONVERT.getInstance().convert(word, appendFlag);
    }

    /**
     * 字符串拼音转换, 并且返回汉字拼音首字母
     *
     * @param word       需要转换的汉字
     * @param appendFlag 需要包含的字符: 数字,空格等字符标记位
     * @return 转换结果 {@link Map.Entry#getKey()} 拼音转换结果, {@link Map.Entry#getValue()} 拼音首字母
     * @see NlpConst#APPEND_CHAR_WHITESPACE
     * @see NlpConst#APPEND_CHAR_LETTER
     * @see NlpConst#APPEND_CHAR_DIGIT
     */
    public static Map.Entry<String, String> firstLetterConvert(char[] word, int appendFlag) {
        return PINYIN_CONVERT.getInstance().firstLetterConvert(word, appendFlag);
    }

    /**
     * 加载词库文件, 通过{@link StandardCharsets#UTF_8}编码打开文件
     *
     * @param filename   词库加载
     * @param lineHandle 每行的处理函数, 入参String: 一行内容, 出参Boolean: true 继续, false 停止后续加载
     */
    public static void loadLexicon(String filename, Function<String, Boolean> lineHandle) {
        loadLexicon(filename, lineHandle, false);
    }

    /**
     * 加载词库文件, 通过{@link StandardCharsets#UTF_8}编码打开文件
     *
     * @param filename   词库加载
     * @param lineHandle 每行的处理函数, 入参String: 一行内容, 出参Boolean: true 继续, false 停止后续加载
     * @param lineTrim   每行数据是否需要trim处理, 即使该值为false, 也会判断line是否为空{@link String#isEmpty()}
     *                   作为词库文件, 都应该尽可能的稍作一些字符串的处理操作
     */
    public static void loadLexicon(String filename, Function<String, Boolean> lineHandle, boolean lineTrim) {
        if (filename.charAt(0) != '/') {
            filename = '/' + filename;
        }
        log.info("开始加载词库文件: " + filename);
        try {
            int lineCount = loadLexicon(NlpUtils.class.getResourceAsStream(filename), lineHandle, lineTrim);
            log.info("加载词库文件: " + filename + " 完成, 共加载了" + lineCount + "行");
        } catch (IOException e) {
            log.error("加载词库文件: " + filename + "时存在异常", e);
            throw new LoadLexiconException("加载词库文件" + filename + "发生异常", e);
        }
    }

    /**
     * 加载词库文件, 通过{@link StandardCharsets#UTF_8}编码打开文件
     *
     * @param in         input输入流, 加载完会执行关闭
     * @param lineHandle 每行的处理函数, 入参String: 一行内容, 出参Boolean: true 继续, false 停止后续加载
     * @param lineTrim   每行数据是否需要trim处理, 即使该值为false, 也会判断line是否为空{@link String#isEmpty()}
     *                   作为词库文件, 都应该尽可能的稍作一些字符串的处理操作
     * @return 加载的行数统计
     * @throws IOException 读取文件发生异常
     */
    public static int loadLexicon(InputStream in, Function<String, Boolean> lineHandle, boolean lineTrim) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            int lineCount = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || (lineTrim && (line = line.trim()).isEmpty())) continue;
                //注释跳过
                if (line.charAt(0) == '#') continue;
                //如果通知, 那就走
                lineCount++;
                if (!lineHandle.apply(line)) break;
            }
            return lineCount;
        }
    }

}
