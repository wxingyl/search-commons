package com.tqmall.search.commons.condition.expression;

/**
 * Created by xing on 16/3/30.
 * 单个条件表达式的额外信息, 比如 左右括号数量, 下一个条件是否为and条件
 * construct this class object suggest use {@link #valueOf(int, int, boolean)}
 *
 * @author xing
 */
public class TokenExtInfo {

    public static final TokenExtInfo ZERO_PARENTHESIS_AND = new TokenExtInfo(0, 0, true);

    public static final TokenExtInfo ZERO_PARENTHESIS_OR = new TokenExtInfo(0, 0, false);
    /**
     * 左括号数量
     */
    private final int leftParenthesisCount;
    /**
     * 右括号数量
     */
    private final int rightParenthesisCount;
    /**
     * 下一个条件是否为and条件
     */
    private final boolean nextAnd;

    TokenExtInfo(int leftParenthesisCount, int rightParenthesisCount, boolean nextAnd) {
        this.leftParenthesisCount = leftParenthesisCount;
        this.rightParenthesisCount = rightParenthesisCount;
        this.nextAnd = nextAnd;
    }

    public static TokenExtInfo valueOf(int leftParenthesisCount, int rightParenthesisCount, boolean nextAnd) {
        if (leftParenthesisCount == 0 && rightParenthesisCount == 0) {
            return nextAnd ? ZERO_PARENTHESIS_AND : ZERO_PARENTHESIS_OR;
        } else {
            return new TokenExtInfo(leftParenthesisCount, rightParenthesisCount, nextAnd);
        }
    }

    public int getLeftParenthesisCount() {
        return leftParenthesisCount;
    }

    public boolean isNextAnd() {
        return nextAnd;
    }

    public int getRightParenthesisCount() {
        return rightParenthesisCount;
    }

    /**
     * only for junit test
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenExtInfo)) return false;

        TokenExtInfo that = (TokenExtInfo) o;

        if (leftParenthesisCount != that.leftParenthesisCount) return false;
        if (rightParenthesisCount != that.rightParenthesisCount) return false;
        return nextAnd == that.nextAnd;

    }

    /**
     * only for junit test
     */
    @Override
    public int hashCode() {
        int result = leftParenthesisCount;
        result = 31 * result + rightParenthesisCount;
        result = 31 * result + (nextAnd ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return Integer.toString(leftParenthesisCount) + ',' + rightParenthesisCount + ',' + nextAnd;
    }
}
