package com.tqmall.search.commons.nlp;

/**
 * Created by xing on 16/2/10.
 * 单个字符匹配结果
 */
public class MatchCharacter {

    private char c;

    /**
     * 在原始text中的位置
     */
    private int srcPos;

    public MatchCharacter(char c, int srcPos) {
        this.c = c;
        this.srcPos = srcPos;
    }

    public char getCharacter() {
        return c;
    }

    public int getSrcPos() {
        return srcPos;
    }

    @Override
    public String toString() {
        return c + ":" + srcPos;
    }
}
