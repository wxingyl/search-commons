package com.tqmall.search.commons.analyzer;

/**
 * Created by xing on 16/3/10.
 * 词类型枚举定义
 *
 * @author xing
 */
public enum TokenType {
    /**
     * 无法识别的词
     */
    UNKNOWN("un"),
    /**
     * 阿拉伯数字
     */
    NUM("n"),
    /**
     * 小数
     */
    DECIMAL("d"),
    /**
     * 英文单词
     */
    EN("e"),
    /**
     * 通过'-'连接的英文混合词, '-'左边是英文字符才会识别为英文混合词
     */
    EN_MIX("em"),
    /**
     * 汉字
     */
    CN("c"),
    /**
     * 量词
     */
    QUANTIFIER("q"),
    /**
     * 数量词
     */
    NUM_QUANTIFIER("nq");

    /**
     * 简称
     */
    private final String simpleName;

    TokenType(String simpleName) {
        this.simpleName = simpleName;
    }

    /**
     * 通过字符串解析{@link TokenType}, 可以通过{@link #simpleName}或者枚举变量名, 或者根据枚举的{@link Enum#ordinal()}, 不区分大小写
     *
     * @param name 可以为: {@link #simpleName}, 枚举变量名, 或者{@link Enum#ordinal()}, 不区分大小写
     * @return 对应的TokenType对象, 没有返回null
     */
    public static TokenType fromString(String name) {
        for (TokenType t : values()) {
            if (t.simpleName.equalsIgnoreCase(name)
                    || t.name().equalsIgnoreCase(name)) return t;
        }
        try {
            int order = Integer.parseInt(name);
            if (order >= 0 && order < values().length) return values()[order];
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}
