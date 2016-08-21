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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collection;

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
    public static boolean isCjkChar(char c) {
        return c >= NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST && c <= NlpConst.CJK_UNIFIED_IDEOGRAPHS_LAST;
    }

    /**
     * 全角字符转半角
     *
     * @return 如果是全角, 则返回对应字符, 如果不是全角则返回自身
     */
    public static char fullWidthConvert(char c) {
        //空格特殊处理
        if (c == '\u3000') {
            return '\u0020';
        } else if (c > '\uFF00' && c < '\uFF5F') {
            return (char) (c - 65248);
        } else {
            return c;
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

    public static void reverseCharArray(char[] text, int off, int len) {
        int n = (off << 1) + len - 1;
        for (int i = ((len - 2) >> 1) + off; i >= off; --i) {
            char tmp = text[i];
            text[i] = text[n - i];
            text[n - i] = tmp;
        }
    }

    /**
     * 将给定的字符数组反转, 我们字符串全部为UTF-8编码, 就不考虑{@link Character#MIN_SURROGATE}, {@link Character#MAX_SURROGATE}
     */
    public static void reverseCharArray(char[] text) {
        int n = text.length - 1;
        for (int i = (n - 1) >> 1; i >= 0; --i) {
            char tmp = text[i];
            text[i] = text[n - i];
            text[n - i] = tmp;
        }
    }

    public static String reverseString(String str) {
        if (SearchStringUtils.isEmpty(str) || str.length() == 1) {
            return str;
        } else {
            char[] array = str.toCharArray();
            reverseCharArray(array);
            return new String(array);
        }
    }

    /**
     * 获取指定class的{@link Class#getResource(String)} 对应文件的{@link Path}对象
     */
    public static Path getPathOfClass(Class cls, String filename) {
        URL uri = cls.getResource(filename);
        try {
            return uri == null ? null : Paths.get(uri.toURI());
        } catch (URISyntaxException e) {
           return null;
        }
    }

    /**
     * @param lineHandle 每行的处理函数, 入参String: 一行内容, 出参Boolean: true 继续, false 停止后续加载
     * @return 加载的行数统计
     * @throws LoadLexiconException 加载词库, 读取文件时发生{@link IOException}, 则抛出{@link LoadLexiconException}, 其为{@link RuntimeException}, 包装了{@link IOException}
     */
    public static int loadClassPathLexicon(Class cls, String filename, Function<String, Boolean> lineHandle) {
        log.info("start load class: " + cls + " path lexicon file: " + filename);
        long start = System.currentTimeMillis();
        try (InputStream in = cls.getResourceAsStream(filename)) {
            int lineCount = loadLexicon(lineHandle, in);
            log.info("load class: " + cls + " path lexicon file: " + filename + " finish, total load " + lineCount + " lines"
                    + ", cost: " + (System.currentTimeMillis() - start) + "ms");
            return lineCount;
        } catch (IOException e) {
            log.error("load class: " + cls + " path lexicon file: " + filename + " have exception", e);
            throw new LoadLexiconException("load class: " + cls + " path lexicon file: " + filename + " have exception", e);
        }
    }

    /**
     * @param lineHandle 每行的处理函数, 入参String: 一行内容, 出参Boolean: true 继续, false 停止后续加载
     * @return 加载的行数统计
     * @throws LoadLexiconException 加载词库, 读取文件时发生{@link IOException}, 则抛出{@link LoadLexiconException}, 其为{@link RuntimeException}, 包装了{@link IOException}
     */
    public static long loadLexicon(Function<String, Boolean> lineHandle, Collection<Path> lexiconPaths) {
        long lineCount = 0L;
        for (Path path : lexiconPaths) {
            lineCount += loadLexicon(lineHandle, path);
        }
        return lineCount;
    }

    /**
     * @param lineHandle 每行的处理函数, 入参String: 一行内容, 出参Boolean: true 继续, false 停止后续加载
     * @return 加载的行数统计
     * @throws LoadLexiconException 加载词库, 读取文件时发生{@link IOException}, 则抛出{@link LoadLexiconException}, 其为{@link RuntimeException}, 包装了{@link IOException}
     */
    public static int loadLexicon(Function<String, Boolean> lineHandle, Path lexiconPath) {
        log.info("start load lexicon file: " + lexiconPath);
        long start = System.currentTimeMillis();
        try (InputStream in = Files.newInputStream(lexiconPath, StandardOpenOption.READ)) {
            int lineCount = loadLexicon(lineHandle, in);
            log.info("load lexicon file: " + lexiconPath + " finish, total load " + lineCount + " lines"
                    + ", cost: " + (System.currentTimeMillis() - start) + "ms");
            return lineCount;
        } catch (IOException e) {
            log.error("load lexicon file: " + lexiconPath + " have exception", e);
            throw new LoadLexiconException("load lexicon file: " + lexiconPath + " have exception", e);
        }
    }

    /**
     * @param lineHandle 每行的处理函数, 入参String: 一行内容, 出参Boolean: true 继续, false 停止后续加载
     * @return 加载的行数统计
     */
    public static int loadLexicon(Function<String, Boolean> lineHandle, InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            int lineCount = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || (line = line.trim()).isEmpty()) continue;
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
