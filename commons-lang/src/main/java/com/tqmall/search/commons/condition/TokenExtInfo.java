package com.tqmall.search.commons.condition;

/**
 * Created by xing on 16/3/30.
 * 单个条件表达式的额外信息, 比如 左右括号数量, 下一个条件是否为and条件
 *
 * @author xing
 */
public class TokenExtInfo {
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

    public TokenExtInfo(int leftParenthesisCount, int rightParenthesisCount, boolean nextAnd) {
        this.leftParenthesisCount = leftParenthesisCount;
        this.rightParenthesisCount = rightParenthesisCount;
        this.nextAnd = nextAnd;
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
}
