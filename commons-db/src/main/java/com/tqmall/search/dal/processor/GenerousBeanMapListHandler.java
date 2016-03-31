/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tqmall.search.dal.processor;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.AbstractKeyedHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * BeanMapHandler中的构造函数是私有的,所以自己实现,此类返回的value是一个list,多值,单值情况下使用{@link GenerousBeanMapHandler}
 *
 * @see org.apache.commons.dbutils.handlers.BeanMapHandler
 */
public class GenerousBeanMapListHandler<K, V> extends AbstractKeyedHandler<K, V> {
    /**
     * The Class of beans produced by this handler.
     */
    private final Class<V> type;

    /**
     * The RowProcessor implementation to use when converting rows into Objects.
     */
    private final RowProcessor convert;

    /**
     * The column index to retrieve key values from. Defaults to 1.
     */
    private final int columnIndex;

    /**
     * The column name to retrieve key values from. Either columnName or
     * columnIndex will be used but never both.
     */
    private final String columnName;

    /**
     * Private Helper
     *
     * @param convert     The <code>RowProcessor</code> implementation to use when
     *                    converting rows into Beans
     * @param columnIndex The values to use as keys in the Map are retrieved from the
     *                    column at this index.
     * @param columnName  The values to use as keys in the Map are retrieved from the
     *                    column with this name.
     */
    public GenerousBeanMapListHandler(Class<V> type, RowProcessor convert,
                                      int columnIndex, String columnName) {
        super();
        this.type = type;
        this.convert = convert;
        this.columnIndex = columnIndex;
        this.columnName = columnName;
    }

    /**
     * This factory method is called by <code>handle()</code> to retrieve the
     * key value from the current <code>ResultSet</code> row.
     *
     * @param rs ResultSet to create a key from
     * @return K from the configured key column name/index
     * @throws SQLException       if a database access error occurs
     * @throws ClassCastException if the class datatype does not match the column type
     * @see AbstractKeyedHandler#createKey(ResultSet)
     */
    // We assume that the user has picked the correct type to match the column
    // so getObject will return the appropriate type and the cast will succeed.
    @SuppressWarnings("unchecked")
    @Override
    protected K createKey(ResultSet rs) throws SQLException {
        return (columnName == null) ?
                (K) rs.getObject(columnIndex) :
                (K) rs.getObject(columnName);
    }

    @Override
    protected V createRow(ResultSet rs) throws SQLException {
        return this.convert.toBean(rs, type);
    }

    @Override
    protected Map<K, V> createMap() {
        return new MultiValueMap();
    }
}
