package com.tqmall.search.commons.nlp;

/**
 * Created by xing on 16/2/10.
 * 单个字符匹配结果
 */
public class MatchCharacter implements Comparable<MatchCharacter> {

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

    @Override
    public int compareTo(MatchCharacter o) {
        return Integer.compare(srcPos, o.srcPos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchCharacter)) return false;

        MatchCharacter that = (MatchCharacter) o;

        return srcPos == that.srcPos;
    }

    @Override
    public int hashCode() {
        return srcPos;
    }
}
