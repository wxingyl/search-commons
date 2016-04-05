package com.tqmall.search.commons.condition.expression;

import com.tqmall.search.commons.condition.*;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.*;

/**
 * Created by xing on 16/3/31.
 * 表达式节点
 *
 * @author xing
 */
public class ConditionExpression implements StrValueConvert<ConditionContainer> {

    public static final ConditionExpression INSTANCE = new ConditionExpression();

    private List<Condition> tokens;

    private TokenExtInfo[] extInfo;

    /**
     * 保证线程安全
     *
     * @param expression 表达式
     * @return 条件容器
     */
    @Override
    public synchronized ConditionContainer convert(String expression) {
        List<ExpressionToken> ets = ExpressionToken.resolveSentence(expression);
        if (CommonsUtils.isEmpty(ets)) {
            throw new IllegalArgumentException("expression: " + expression + " is invalid");
        }
        final int size = ets.size();
        tokens = new ArrayList<>(size);
        extInfo = new TokenExtInfo[size];
        try {
            for (int i = 0; i < size; i++) {
                tokens.add(Resolvers.resolveCondition(ets.get(i)));
                extInfo[i] = ets.get(i).getTokenExtInfo();
            }
            return buildContainer();
        } finally {
            tokens = null;
            extInfo = null;
        }
    }

    private ConditionContainer buildContainer() {
        Deque<Integer> deque = new LinkedList<>();
        int index = 0;
        ListIterator<Condition> it = tokens.listIterator();
        //去掉括号
        while (it.hasNext()) {
            it.next();
            int lc = extInfo[index].getLeftParenthesisCount();
            while (lc > 0) {
                deque.addLast(index);
                lc--;
            }
            lc = extInfo[index].getRightParenthesisCount();
            int lastStart = -1;
            while (lc > 0) {
                int start = deque.pollLast();
                if (lastStart != start) {
                    lastStart = start;
                    List<Condition> childList = tokens.subList(start, index + 1);
                    ConditionContainer container = childCreate(childList, start);
                    int shiftNum = extInfo.length - index;
                    System.arraycopy(extInfo, index, extInfo, start, shiftNum);
                    while (index > start) {
                        it.remove();
                        it.previous();
                        index--;
                    }
                    it.set(container);
                    it.next();
                }
                lc--;
            }
            index++;
        }
        if (tokens.size() == 1 && tokens.get(0) instanceof ConditionContainer) {
            return (ConditionContainer) tokens.get(0);
        } else {
            return childCreate(tokens, 0);
        }
    }

    private ConditionContainer childCreate(List<Condition> tokens, final int offset) {
        UnmodifiableConditionContainer.Builder builder = Conditions.unmodifiableContainer();
        boolean haveAdd = false;
        int loopEnd = tokens.size() - 1;
        for (int i = 0; i < loopEnd; i++) {
            if (extInfo[i + offset].isNextAnd()) {
                builder.mustCondition(tokens.get(i));
                haveAdd = true;
            } else if (haveAdd) {
                builder.mustCondition(tokens.get(i));
                Condition condition = builder.create();
                builder = Conditions.unmodifiableContainer();
                builder.shouldCondition(condition);
                haveAdd = false;
            } else {
                builder.shouldCondition(tokens.get(i));
            }
        }
        if (haveAdd || loopEnd == 0) {
            builder.mustCondition(tokens.get(loopEnd));
        } else {
            builder.shouldCondition(tokens.get(loopEnd));
        }
        return builder.create();
    }
}
