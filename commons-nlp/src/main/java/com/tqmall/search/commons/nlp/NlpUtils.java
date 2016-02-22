package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.exception.LoadLexiconException;
import com.tqmall.search.commons.lang.LazyInit;
import com.tqmall.search.commons.lang.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by xing on 16/1/26.
 * Nlp 操作Utils类
 */
public final class NlpUtils {

    private static final Logger log = LoggerFactory.getLogger(NlpUtils.class);

    /**
     * 标点符号列表
     */
    private static final Set<Character> PUNCTUATIONS_SET;

    static {
        PUNCTUATIONS_SET = new HashSet<>();
        for (char ch : "`~!@#$%^&*()_+-={}|[]\\:\";'<>?,./·~！@#￥%……&*（）——+-={}|【】、：“；‘《》？，。、".toCharArray()) {
            PUNCTUATIONS_SET.add(ch);
        }
    }

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
            String msg = "加载拼音词库完成, 耗时: " + (System.currentTimeMillis() - startTime) + "ms";
            log.info(msg);
            System.out.println(msg);
            return convert;
        }
    });

    /**
     * 分词实例
     */
    private static final LazyInit<Segment> SEGMENT = new LazyInit<>(new Supplier<Segment>() {
        @Override
        public Segment get() {
            log.info("开始初始化分词实例, 加载词库");
            long startTime = System.currentTimeMillis();
            Segment segment = new Segment();
            String msg = "初始化分词实例, 加载词库完成, 耗时: " + (System.currentTimeMillis() - startTime) + "ms";
            log.info(msg);
            System.out.println(msg);
            return segment;
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
     * 是否为特殊字符, 主要判断:
     * 1. 是否一些常用标点, 即{@link #PUNCTUATIONS_SET}
     * 2. 判断{@link Character.UnicodeBlock}是否为{@link Character.UnicodeBlock#CJK_SYMBOLS_AND_PUNCTUATION}
     * 或者{@link Character.UnicodeBlock#GENERAL_PUNCTUATION}
     * 判断字符是否为标点符号
     */
    public static boolean isSpecialChar(char ch) {
        if (PUNCTUATIONS_SET.contains(ch)) {
            return true;
        }
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
        return ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
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
    public static String pyCjkConvert(char cjkChar) {
        return PINYIN_CONVERT.getInstance().cjkConvert(cjkChar);
    }

    /**
     * 只将汉字转换为对应拼音, 其他未识别字符都忽略
     *
     * @param word             需要转换的汉字
     * @param ignoreWhitespace 是否忽略空白符, 如果不忽略则保留
     */
    public static String pyNormalConvert(String word, boolean ignoreWhitespace) {
        Map.Entry<String, String> e = PINYIN_CONVERT.getInstance().normalConvert(word, ignoreWhitespace, false);
        return e == null ? null : e.getKey();
    }

    /**
     * 将汉字转换为对应拼音以及首字母字符串, 其他未识别字符都忽略
     *
     * @param word             需要转换的汉字
     * @param ignoreWhitespace 是否忽略空白符, 如果不忽略则保留
     * @return {@link Map.Entry#getKey()} 为转换的拼音text, {@link Map.Entry#getValue()} 为拼音首字母字符串
     */
    public static Map.Entry<String, String> pyNormalFirstLetterConvert(String word, boolean ignoreWhitespace) {
        return PINYIN_CONVERT.getInstance().normalConvert(word, ignoreWhitespace, true);
    }

    /**
     * 输入的汉字转换为拼音, 拼配结果中包括: 转换完的拼音字符串, 每个汉字的拼音首字母字符串以及未能识别的字符列表(分为cjk和非cjk)
     * 注意: 如果没有一个拼音匹配, 则返回null, 不再做任何处理
     *
     * @param word 需要转换的汉字
     * @return 转换结果, 如果没有一个拼音匹配, 则返回null, 不再做任何处理
     */
    public static PinyinConvert.Result pyFullConvert(String word) {
        return PINYIN_CONVERT.getInstance().fullConvert(word);
    }

    /**
     * 索引分词, 尽可能的返回所有分词结果
     *
     * @param text 待分词文本
     * @return 分词结果
     */
    public static List<Hit<Void>> fullSegmentText(String text) {
        return SEGMENT.getInstance().fullSegment(text);
    }

    /**
     * 最大分词匹配
     *
     * @param text 待输入文本
     * @return 最大分词结果
     */
    public static List<Hit<Void>> maxSegmentText(String text) {
        return SEGMENT.getInstance().maxSegment(text);
    }


    /**
     * 最小分词匹配
     *
     * @param text 待输入文本
     * @return 最小分词结果
     */
    public static List<Hit<Void>> minSegmentText(String text) {
        return SEGMENT.getInstance().minSegment(text);
    }

    /**
     * 加载词库文件, 通过{@link StandardCharsets#UTF_8}编码打开文件
     *
     * @param filename   词库加载
     * @param lineHandle 每行的处理函数
     */
    public static void loadLexicon(String filename, LineHandle lineHandle) {
        loadLexicon(filename, lineHandle, false);
    }

    /**
     * 加载词库文件, 通过{@link StandardCharsets#UTF_8}编码打开文件
     *
     * @param filename   词库加载
     * @param lineHandle 每行的处理函数
     * @param lineTrim   每行数据是否需要trim处理, 即使该值为false, 也会判断line是否为空{@link String#isEmpty()}
     *                   作为词库文件, 都应该尽可能的稍作一些字符串的处理操作
     */
    public static void loadLexicon(String filename, LineHandle lineHandle, boolean lineTrim) {
        if (filename.charAt(0) != '/') {
            filename = '/' + filename;
        }
        log.info("开始加载词库文件: " + filename);
        try {
            int lineCount = loadLexicon(NlpUtils.class.getResourceAsStream(filename), lineHandle, lineTrim);
            log.info("加载词库文件: " + filename + " 完成, 共加载了" + lineCount + "行");
        } catch (IOException e) {
            log.error("加载词库文件: " + filename + "时存在异常", e);
            throw new LoadLexiconException(filename, e);
        }
    }

    /**
     * 加载词库文件, 通过{@link StandardCharsets#UTF_8}编码打开文件
     *
     * @param in         input输入流, 加载完会执行关闭
     * @param lineHandle 每行的处理函数
     * @param lineTrim   每行数据是否需要trim处理, 即使该值为false, 也会判断line是否为空{@link String#isEmpty()}
     *                   作为词库文件, 都应该尽可能的稍作一些字符串的处理操作
     * @return 加载的行数统计
     * @throws IOException 读取文件发生异常
     */
    public static int loadLexicon(InputStream in, LineHandle lineHandle, boolean lineTrim) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            int lineCount = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || (lineTrim && (line = line.trim()).isEmpty())) continue;
                //注释跳过
                if (line.charAt(0) == '#') continue;
                //如果通知, 那就走
                lineCount++;
                if (!lineHandle.onHandle(line)) break;
            }
            return lineCount;
        }
    }

    /**
     * 行处理接口
     */
    public interface LineHandle {

        /**
         * @param line 一行内容
         * @return true 继续, false 停止后续加载
         */
        boolean onHandle(String line);
    }

}
