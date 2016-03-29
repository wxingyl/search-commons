package com.tqmall.search.commons.param.condition;

import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.param.Param;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.SearchStringUtils;
import com.tqmall.search.commons.utils.StrValueConverts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xing on 16/3/29.
 * {@link FieldCondition}相关工具类
 *
 * @author xing
 */
public final class Conditions {

    private Conditions() {
    }

    /**
     * 该build方法对传入的values做了null过滤
     * 构造{@link InCondition}
     *
     * @param values 如果不为空, 做完{@link CommonsUtils#filterNullValue(List)}过滤为空抛出{@link IllegalArgumentException}
     * @return 如果values为空, 返回null
     * @see CommonsUtils#filterNullValue(List)
     */
    public static <T> InCondition<T> in(String field, List<T> values, Class<T> cls) {
        if (CommonsUtils.isEmpty(values)) return null;
        return new InCondition<>(field, values, StrValueConverts.getConvert(cls));
    }

    /**
     * 该build方法对传入的values做了null过滤
     * 构造{@link InCondition}
     *
     * @param values 如果不为空, 做完{@link CommonsUtils#filterNullValue(List)}过滤为空抛出{@link IllegalArgumentException}
     * @return 如果values为空, 返回null
     * @see CommonsUtils#filterNullValue(List)
     */
    @SafeVarargs
    public static <T> InCondition<T> in(String field, Class<T> cls, T... values) {
        if (values.length == 0) return null;
        return new InCondition<>(field, Arrays.asList(values), StrValueConverts.getConvert(cls));
    }

    /**
     * @param value 不可以为null
     */
    @SuppressWarnings({"rawstype", "unchecked"})
    public static <T> EqualCondition<T> equal(String field, T value) {
        return new EqualCondition<>(field, value, StrValueConverts.getBasicConvert((Class<T>) value.getClass()));
    }

    /**
     * @param value 可以为null
     */
    public static <T> EqualCondition<T> equal(String field, T value, Class<T> cls) {
        return new EqualCondition<>(field, value, StrValueConverts.getBasicConvert(cls));
    }

    public static <T extends Comparable<T>> RangeCondition.Builder<T> range(String field) {
        return new RangeCondition.Builder<>(field);
    }

    public static <T extends Comparable<T>> RangeCondition.Builder<T> range(String field, Class<T> cls) {
        return new RangeCondition.Builder<>(field, StrValueConverts.getConvert(cls));
    }

    public static <T extends Comparable<T>> RangeCondition.Builder<T> range(String field, StrValueConvert<T> convert) {
        return new RangeCondition.Builder<>(field, convert);
    }

    public static <T extends Comparable<T>> RangeCondition<T> range(String field, String rangeStr, Class<T> cls) {
        return range(field, rangeStr, StrValueConverts.getBasicConvert(cls));
    }

    /**
     * @param field        range的字段, 不能为Null
     * @param rangeStr     range区间字符串, 如果isEmpty, return null.
     * @param valueConvert 值转化器
     * @param <T>          对应类型
     * @return 构造好的RangeFilter对象
     */
    public static <T extends Comparable<T>> RangeCondition<T>
    range(String field, String rangeStr, StrValueConvert<T> valueConvert) {
        if (rangeStr == null || rangeStr.isEmpty()) return null;
        String[] rangeArray = SearchStringUtils.split(rangeStr, Param.RANGE_FILTER_CHAR);
        if (rangeArray.length == 0) return null;
        rangeArray = SearchStringUtils.stringArrayTrim(rangeArray);
        int startIndex = 0, endIndex = 1;
        if (rangeArray.length == 1) {
            if (rangeStr.charAt(0) == Param.RANGE_FILTER_CHAR) {
                startIndex = -1;
                endIndex = 0;
            } else {
                startIndex = 0;
                endIndex = -1;
            }
        }
        T startValue = null, endValue = null;
        if (startIndex == 0 && rangeArray[0] != null && '*' != rangeArray[0].charAt(0)) {
            startValue = valueConvert.convert(rangeArray[0]);
        }
        if (endIndex >= 0 && rangeArray[endIndex] != null && '*' != rangeArray[endIndex].charAt(0)) {
            endValue = valueConvert.convert(rangeArray[endIndex]);
        }
        return new RangeCondition<>(field, startValue, false, endValue, false, valueConvert);
    }

    public static UnmodifiableConditionContainer.Builder unmodifiableContainer() {
        return new UnmodifiableConditionContainer.Builder();
    }

    public static ModifiableConditionContainer modifiableContainer() {
        return new ModifiableConditionContainer();
    }

    /**
     * 解析条件表达式
     *
     * @param conditionalExpression 条件表达式
     * @return 解析的容器集合对象
     */
    public static ConditionContainer parseConditionalExpression(String conditionalExpression) {
        List<Token> tokenList = createTokens(conditionalExpression);
        return null;
    }

    static List<Token> createTokens(String conditionalExpression) {
        final int loopEnd = conditionalExpression.length() - 4;
        boolean inStrValue = false;
        int lastStart = 0;
        List<Token> tokens = new ArrayList<>();
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
                Token curToken = new Token(lastStart, i, and, text);
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
        Token lastToken = new Token(lastStart, conditionalExpression.length(), false, text);
        tokens.add(lastToken);
        leftParenthesisCount += lastToken.leftParenthesisCount;
        rightParenthesisCount += lastToken.rightParenthesisCount;
        if (leftParenthesisCount != rightParenthesisCount) {
            throw new IllegalArgumentException("conditionalExpression: " + conditionalExpression + " not correct match '{' '}', leftParenthesisCount: "
                    + leftParenthesisCount + ", rightParenthesisCount: " + rightParenthesisCount);
        }
        return tokens;
    }

    static class Token {
        final boolean nextAnd;
        final int leftParenthesisCount;
        final int rightParenthesisCount;
        final Operator op;
        final String field;
        final String value;

        Token(int startPos, int endPos, boolean nextAnd, char[] text) {
            this.nextAnd = nextAnd;
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
            this.leftParenthesisCount = leftParenthesisCount;
            this.rightParenthesisCount = rightParenthesisCount;
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
            this.value = String.valueOf(text, lastStart, endPos - lastStart);
            this.field = field;
            this.op = op;
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
