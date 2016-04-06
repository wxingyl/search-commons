package com.tqmall.search.db.param;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/4/5.
 *
 * @author xing
 */
public class BeanQueryParam<T> extends QueryParam {

    private final Class<T> cls;

    private List<String> exceptFields;

    public BeanQueryParam(String schema, String table, int size, Class<T> cls) {
        super(schema, table, size);
        this.cls = cls;
    }

    public BeanQueryParam<T> exceptFields(String... fields) {
        if (exceptFields == null) {
            exceptFields = new ArrayList<>();
        }
        Collections.addAll(exceptFields, fields);
        return this;
    }

    @Override
    protected void appendSqlStatementOfFields(StringBuilder sql) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(cls);
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Bean: " + cls + " introspection failed", e);
        }
        boolean findColumn = false;
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (pd.getWriteMethod() == null || (exceptFields != null && exceptFields.contains(pd.getName()))) {
                continue;
            }
            if (!findColumn) findColumn = true;
            SqlStatements.appendField(sql, SqlStatements.toUnderscoreCase(pd.getName())).append(", ");
        }
        if (findColumn) {
            sql.delete(sql.length() - 2, sql.length());
        } else {
            throw new IllegalArgumentException("bean class: " + cls + ", exceptFields: " + exceptFields + " can not find query column");
        }
    }


}
