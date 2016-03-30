package com.tqmall.search.commons.param.condition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/3/30.
 *
 * @author xing
 */
public class ExpressionToken {
    /**
     * 左括号数量
     */
    private final int leftParenthesisCount;
    /**
     * 字段名
     */
    private final String field;
    /**
     * 操作符号
     */
    private final Operator op;
    /**
     * 表达式的value部分
     */
    private final String value;
    /**
     * 右括号数量
     */
    private final int rightParenthesisCount;
    /**
     * 下一个条件是否为and操作
     */
    private final boolean nextAnd;


    ExpressionToken(int leftParenthesisCount, String field, Operator op, String value,
                    int rightParenthesisCount, boolean nextAnd) {
        this.leftParenthesisCount = leftParenthesisCount;
        this.field = field;
        this.op = op;
        this.value = value;
        this.rightParenthesisCount = rightParenthesisCount;
        this.nextAnd = nextAnd;
    }

    public String getField() {
        return field;
    }

    public int getLeftParenthesisCount() {
        return leftParenthesisCount;
    }

    public boolean isNextAnd() {
        return nextAnd;
    }

    public Operator getOp() {
        return op;
    }

    public int getRightParenthesisCount() {
        return rightParenthesisCount;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leftParenthesisCount; i++) {
            sb.append('(');
        }
        sb.append(' ').append(field).append(' ').append(op.getOp()).append(' ').append(value).append(' ');
        for (int i = 0; i < rightParenthesisCount; i++) {
            sb.append(')');
        }
        if (rightParenthesisCount > 0) {
            sb.append(' ');
        }
        sb.append(nextAnd);
        return sb.toString();
    }

    /**
     * only for junit test
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpressionToken)) return false;

        ExpressionToken that = (ExpressionToken) o;

        if (leftParenthesisCount != that.leftParenthesisCount) return false;
        if (rightParenthesisCount != that.rightParenthesisCount) return false;
        if (nextAnd != that.nextAnd) return false;
        if (!field.equals(that.field)) return false;
        if (op != that.op) return false;
        return value.equals(that.value);
    }

    /**
     * only for junit test
     */
    @Override
    public int hashCode() {
        int result = leftParenthesisCount;
        result = 31 * result + field.hashCode();
        result = 31 * result + op.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + rightParenthesisCount;
        result = 31 * result + (nextAnd ? 1 : 0);
        return result;
    }

    /**
     * 解析条件表达式句子
     *
     * @param conditionalExpression 条件表达式句子
     * @return 表达式Token
     */
    public static List<ExpressionToken> parseSentence(String conditionalExpression) {
        final int loopEnd = conditionalExpression.length() - 4;
        boolean inStrValue = false;
        int lastStart = 0;
        List<ExpressionToken> tokens = new ArrayList<>();
        final char[] text = conditionalExpression.toCharArray();
        int leftParenthesisCount = 0, rightParenthesisCount = 0;
        for (int i = 0; i < loopEnd; i++) {
            if (text[i] == '"') {
                inStrValue = !inStrValue;
            }
            if (inStrValue) continue;
            if (text[i] == ' ' && text[i + 3] == ' ') {
                boolean and;
                if (text[i + 1] == '|' && text[i + 2] == '|') {
                    and = false;
                } else if (text[i + 1] == '&' && text[i + 2] == '&') {
                    and = true;
                } else {
                    continue;
                }
                ExpressionToken curToken = createExpressionToken(lastStart, i, and, text);
                leftParenthesisCount += curToken.leftParenthesisCount;
                rightParenthesisCount += curToken.rightParenthesisCount;
                tokens.add(curToken);
                lastStart = i + 4;
            }
        }
        if (inStrValue) {
            throw new IllegalArgumentException("conditionalExpression: " + String.valueOf(text) +
                    " have invalid string value, '\"' can not match paired");
        }
        ExpressionToken lastToken = createExpressionToken(lastStart, conditionalExpression.length(), false, text);
        tokens.add(lastToken);
        leftParenthesisCount += lastToken.leftParenthesisCount;
        rightParenthesisCount += lastToken.rightParenthesisCount;
        if (leftParenthesisCount != rightParenthesisCount) {
            throw new IllegalArgumentException("conditionalExpression: " + conditionalExpression + " not correct match '{' '}', leftParenthesisCount: "
                    + leftParenthesisCount + ", rightParenthesisCount: " + rightParenthesisCount);
        }
        return tokens;
    }

    public static ExpressionToken createExpressionToken(int startPos, int endPos, boolean nextAnd, char[] text) {
        int leftParenthesisCount = 0, rightParenthesisCount = 0;
        //fix position
        for (; startPos < endPos; startPos++) {
            if (text[startPos] == '(') {
                leftParenthesisCount++;
                continue;
            }
            if (text[startPos] == ')') {
                throw new IllegalArgumentException("conditionalExpression: " + String.valueOf(text)
                        + " ')' should not find in the head of condition");
            } else if (!Character.isWhitespace(text[startPos])) {
                break;
            }
        }
        for (; startPos < endPos; endPos--) {
            char c = text[endPos - 1];
            if (c == ')') {
                rightParenthesisCount++;
                continue;
            }
            if (c == '(') {
                throw new IllegalArgumentException("conditionalExpression: " + String.valueOf(text)
                        + " '(' should not find in the tail of condition");
            } else if (!Character.isWhitespace(c)) {
                break;
            }
        }
        if (startPos >= endPos || (leftParenthesisCount > 0 && rightParenthesisCount > 0)) {
            throw new IllegalArgumentException("conditionalExpression: " + String.valueOf(text) + " have error expression , start: "
                    + startPos + ", end: " + endPos + ", leftParenthesisCount: " + leftParenthesisCount + ", rightParenthesisCount: "
                    + rightParenthesisCount);
        }
        String field = null;
        int lastStart = 0;
        Operator op = null;
        for (int i = startPos; i < endPos; i++) {
            if (Character.isWhitespace(text[i])) {
                if (field == null) {
                    field = String.valueOf(text, startPos, i - startPos);
                } else if (op == null) {
                    op = Operator.getOp(String.valueOf(text, lastStart, i - lastStart));
                }
                lastStart = i + 1;
            }
        }
        return new ExpressionToken(leftParenthesisCount, field, op, String.valueOf(text, lastStart, endPos - lastStart),
                rightParenthesisCount, nextAnd);
    }

    public enum Operator {
        EQ("="),
        NE("!="),
        GT(">"),
        GE(">="),
        LT("<"),
        LE("<="),
        IN("in"),
        NIN("nin"),
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
}
