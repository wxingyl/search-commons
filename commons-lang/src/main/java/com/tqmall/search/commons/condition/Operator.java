package com.tqmall.search.commons.condition;

/**
 * Created by xing on 16/3/30.
 * 运算符类型定义
 *
 * @author xing
 */
public enum Operator {
    /**
     * 格式: field = XXX
     */
    EQ("="),
    /**
     * 格式: field != XXX
     */
    NE("!="),
    /**
     * 格式: field > XXX
     */
    GT(">"),
    /**
     * 格式: field >= XXX
     */
    GE(">="),
    /**
     * 格式: field < XXX
     */
    LT("<"),
    /**
     * 格式: field <= XXX
     */
    LE("<="),
    /**
     * 格式: field in X1, X2, X3
     */
    IN("in"),
    /**
     * 格式: field nin X1, X2, X3
     */
    NIN("nin"),
    /**
     * 格式:
     * field range leftValue <, < rightValue  表示field (leftValue, rightValue)
     * field range leftValue <=, < rightValue  表示field [leftValue, rightValue)
     * field range leftValue <, <= rightValue  表示field (leftValue, rightValue]
     * field range leftValue <=, <= rightValue  表示field [leftValue, rightValue]
     */
    RANGE("range");

    public static Operator getOp(String opName) {
        for (Operator op : values()) {
            if (op.op.equalsIgnoreCase(opName) || op.name().equalsIgnoreCase(opName)) {
                return op;
            }
        }
        throw new IllegalArgumentException("opName: " + opName + " is invalid");
    }

    private final String op;

    Operator(String op) {
        this.op = op;
    }

    public String getOp() {
        return op;
    }

}
