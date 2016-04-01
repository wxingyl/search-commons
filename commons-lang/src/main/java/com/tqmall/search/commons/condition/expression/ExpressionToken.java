package com.tqmall.search.commons.condition.expression;

import com.tqmall.search.commons.condition.Operator;
import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/3/30.
 * 条件表达式语句初步解析结果
 *
 * @author xing
 */
public class ExpressionToken {
    /**
     * 字段名
     */
    private final String field;
    /**
     * 操作符号
     */
    private final Operator op;
    /**
     * 表达式的value部分, 左右经过term, 保证不为空
     */
    private final String value;

    private final TokenExtInfo tokenExtInfo;

    ExpressionToken(String field, Operator op,
                    String value, TokenExtInfo tokenExtInfo) {
        this.field = field;
        this.op = op;
        this.value = value;
        this.tokenExtInfo = tokenExtInfo;
    }

    public String getField() {
        return field;
    }

    public Operator getOp() {
        return op;
    }

    public TokenExtInfo getTokenExtInfo() {
        return tokenExtInfo;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokenExtInfo.getLeftParenthesisCount(); i++) {
            sb.append('(');
        }
        sb.append(' ').append(field).append(' ').append(op.getOp()).append(' ').append(value).append(' ');
        for (int i = 0; i < tokenExtInfo.getRightParenthesisCount(); i++) {
            sb.append(')');
        }
        if (tokenExtInfo.getRightParenthesisCount() > 0) {
            sb.append(' ');
        }
        sb.append(tokenExtInfo.isNextAnd());
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

        if (!field.equals(that.field)) return false;
        if (op != that.op) return false;
        if (!value.equals(that.value)) return false;
        return tokenExtInfo.equals(that.tokenExtInfo);
    }

    /**
     * only for junit test
     */
    @Override
    public int hashCode() {
        int result = field.hashCode();
        result = 31 * result + op.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + tokenExtInfo.hashCode();
        return result;
    }

    /**
     * 解析条件表达式句子
     *
     * @param conditionalExpression 条件表达式句子
     * @return 表达式Token list按照解析条件表达式的顺序返回 , 最后一个ExpressionToken的{@link TokenExtInfo#nextAnd} = false
     */
    public static List<ExpressionToken> resolveSentence(String conditionalExpression) {
        conditionalExpression = SearchStringUtils.filterString(conditionalExpression);
        if (conditionalExpression == null) return null;
        final int loopEnd = conditionalExpression.length() - 4;
        int lastStart = 0;
        List<ExpressionToken> tokens = new ArrayList<>();
        final char[] text = conditionalExpression.toCharArray();
        int leftParenthesisCount = 0, rightParenthesisCount = 0;
        for (int i = 0; i < loopEnd; i++) {
            if (text[i] == '"') {
                int length = conditionalExpression.length();
                for (int j = i + 1; j < length; j++) {
                    if (text[j] == '"') {
                        i = j;
                        break;
                    }
                }
            } else if (text[i] == ' ' && text[i + 3] == ' ') {
                boolean and;
                if (text[i + 1] == '|' && text[i + 2] == '|') {
                    and = false;
                } else if (text[i + 1] == '&' && text[i + 2] == '&') {
                    and = true;
                } else {
                    continue;
                }
                ExpressionToken curToken = valueOf(lastStart, i, and, text);
                leftParenthesisCount += curToken.getTokenExtInfo().getLeftParenthesisCount();
                rightParenthesisCount += curToken.getTokenExtInfo().getRightParenthesisCount();
                tokens.add(curToken);
                lastStart = i + 4;
            }
        }
        //last condition nextAdd is true
        ExpressionToken lastToken = valueOf(lastStart, conditionalExpression.length(), false, text);
        tokens.add(lastToken);
        leftParenthesisCount += lastToken.getTokenExtInfo().getLeftParenthesisCount();
        rightParenthesisCount += lastToken.getTokenExtInfo().getRightParenthesisCount();
        if (leftParenthesisCount != rightParenthesisCount) {
            throw new IllegalArgumentException("conditionalExpression: " + conditionalExpression + " not correct match '{' '}', leftParenthesisCount: "
                    + leftParenthesisCount + ", rightParenthesisCount: " + rightParenthesisCount);
        }
        return tokens;
    }

    static ExpressionToken valueOf(int startPos, int endPos, final boolean nextAnd, final char[] text) {
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
        if (leftParenthesisCount > 0 && rightParenthesisCount > 0) {
            if (leftParenthesisCount > rightParenthesisCount) {
                leftParenthesisCount -= rightParenthesisCount;
                rightParenthesisCount = 0;
            } else {
                rightParenthesisCount -= leftParenthesisCount;
                leftParenthesisCount = 0;
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
                    lastStart = i + 1;
                } else {
                    op = Operator.getOp(String.valueOf(text, lastStart, i - lastStart));
                    lastStart = i + 1;
                    break;
                }
            }
        }
        if (op == null) {
            throw new IllegalArgumentException("conditionalExpression: " + String.valueOf(text) + ", field: "
                    + field + " have error expression");
        }
        return new ExpressionToken(field, op, String.valueOf(text, lastStart, endPos - lastStart),
                TokenExtInfo.valueOf(leftParenthesisCount, rightParenthesisCount, nextAnd));
    }

}
