package com.tqmall.search.db;

import com.tqmall.search.commons.condition.ConditionContainer;

/**
 * Created by xing on 16/4/5.
 * mysql 语句相关操作工具类
 *
 * @author xing
 */
public class SqlStatements {

    private static final ThreadLocal<ConditionSqlStatement> CONDITION_RESOLVE = new ThreadLocal<ConditionSqlStatement>() {
        @Override
        protected ConditionSqlStatement initialValue() {
            return new ConditionSqlStatement();
        }
    };

    public static StringBuilder appendContainer(StringBuilder sql, ConditionContainer container) {
        if (container != null) {
            CONDITION_RESOLVE.get().appendConditionContainer(sql, container);
        }
        return sql;
    }

    public static StringBuilder appendValue(StringBuilder sql, Object value) {
        if (value instanceof Number) {
            sql.append(value);
        } else if (value == null) {
            sql.append("NULL");
        } else {
            sql.append('\'').append(value).append('\'');
        }
        return sql;
    }

    public static StringBuilder appendField(StringBuilder sql, String field) {
        sql.append('`').append(field).append('`');
        return sql;
    }

    /**
     * 将驼峰命名转换成下划线, 该代码来自elasticsearch中的Strings.java
     *
     * @param value 驼峰命名格式的名称
     * @return 下划线命名规范的名称
     */
    public static String toUnderscoreCase(String value) {
        boolean changed = false;
        StringBuilder sb = null;
        final int length = value.length();
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c)) {
                if (changed) {
                    sb.append('_');
                    sb.append(Character.toLowerCase(c));
                } else {
                    sb = new StringBuilder(length + 8);
                    // copy it over here
                    for (int j = 0; j < i; j++) {
                        sb.append(value.charAt(j));
                    }
                    changed = true;
                    if (i != 0) {
                        sb.append('_');
                    }
                    sb.append(Character.toLowerCase(c));
                }
            } else if (changed) {
                sb.append(c);
            }
        }
        return changed ? sb.toString() : value;
    }

    /**
     * 下划线命名转驼峰, 该代码来自elasticsearch中的Strings.java
     */
    public static String toCamelCase(String value) {
        StringBuilder sb = null;
        boolean changed = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            //e.g. _name stays as-is, _first_name becomes _firstName
            if (c == '_' && i > 0) {
                if (!changed) {
                    sb = new StringBuilder(value.length());
                    // copy it over here
                    for (int j = 0; j < i; j++) {
                        sb.append(value.charAt(j));
                    }
                    changed = true;
                }
                if (i < value.length() - 1) {
                    sb.append(Character.toUpperCase(value.charAt(++i)));
                }
            } else if (changed) {
                sb.append(c);
            }
        }
        return changed ? sb.toString() : value;
    }

}
