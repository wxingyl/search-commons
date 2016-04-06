package com.tqmall.search.db;

import org.apache.commons.dbutils.BeanProcessor;

import java.beans.PropertyDescriptor;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by xing on 16/4/6.
 * 同{@link org.apache.commons.dbutils.GenerousBeanProcessor}, 只不过处理下划线名称时使用{@link SqlStatements#toCamelCase(String)}
 *
 * @author xing
 */
public class MysqlBeanProcessor extends BeanProcessor {

    public MysqlBeanProcessor() {
        super(Collections.<String, String>emptyMap());
    }

    @Override
    protected int[] mapColumnsToProperties(ResultSetMetaData rsmd, PropertyDescriptor[] props) throws SQLException {
        final int cols = rsmd.getColumnCount();
        final int[] columnToProperty = new int[cols + 1];
        Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);
        for (int col = 0; col < cols; col++) {
            String columnName = rsmd.getColumnLabel(col);
            if (null == columnName || 0 == columnName.length()) {
                columnName = rsmd.getColumnName(col);
            }
            final String generousColumnName = SqlStatements.toCamelCase(columnName);

            for (int i = 0; i < props.length; i++) {
                final String propName = props[i].getName();
                // see if either the column name, or the generous one matches
                if (generousColumnName.equalsIgnoreCase(propName) || columnName.equalsIgnoreCase(propName)) {
                    columnToProperty[col] = i;
                    break;
                }
            }
        }
        return columnToProperty;
    }
}
