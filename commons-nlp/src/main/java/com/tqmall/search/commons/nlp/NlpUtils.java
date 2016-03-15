package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.exception.LoadLexiconException;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.utils.SearchStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Created by xing on 16/1/26.
 * Nlp 操作Utils类
 */
public final class NlpUtils {

    private static final Logger log = LoggerFactory.getLogger(NlpUtils.class);

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

    public static char[] stringToCharArray(String key) {
        return SearchStringUtils.isEmpty(key) ? null : key.toCharArray();
    }

    public static void arrayIndexCheck(char[] text, int startPos, int endPos) {
        if (text == null || startPos < 0 || startPos > endPos) {
            throw new ArrayIndexOutOfBoundsException("text.length: " + (text == null ? 0 : text.length) + ", startPos: "
                    + startPos + ", endPos: " + endPos);
        }
    }

    /**
     * 将给定的字符数组反转, 我们字符串全部为UTF-8编码, 就不考虑{@link Character#MIN_SURROGATE}, {@link Character#MAX_SURROGATE}
     */
    public static void reverseCharArray(char[] text) {
        if (text != null) {
            int n = text.length - 1;
            for (int i = (n - 1) >> 1; i >= 0; --i) {
                char tmp = text[i];
                text[i] = text[n - i];
                text[n - i] = tmp;
            }
        }
    }

    public static String reverseString(String str) {
        if (SearchStringUtils.isEmpty(str) || str.length() == 1) return str;
        else {
            char[] array = str.toCharArray();
            reverseCharArray(array);
            return new String(array);
        }
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
        log.info("start load lexicon file: " + filename);
        try (InputStream in = NlpUtils.class.getResourceAsStream(filename)) {
            int lineCount = loadLexicon(in, lineHandle, lineTrim);
            log.info("load lexicon file: " + filename + " finish, total load " + lineCount + " lines");
        } catch (IOException e) {
            log.error("load lexicon file: " + filename + " have exception", e);
            throw new LoadLexiconException("load lexicon file: " + filename + " have exception", e);
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
