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

import org.apache.commons.dbutils.handlers.AbstractKeyedHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @see org.apache.commons.dbutils.handlers.KeyedHandler
 */
public class FreeKeyedHandler<K, V> extends AbstractKeyedHandler<K, V> {

    /**
     * The column index to retrieve key values from.  Defaults to 1.
     */
    protected final int keyIndex;

    /**
     * The column name to retrieve key values from.  Either columnName or
     * columnIndex will be used but never both.
     */
    protected final String keyName;
    /**
     * The column index to retrieve key values from.  Defaults to 1.
     */
    protected final int valueIndex;

    /**
     * The column name to retrieve key values from.  Either columnName or
     * columnIndex will be used but never both.
     */
    protected final String valueName;

    public FreeKeyedHandler(int keyIndex,
                            String keyName, int valueIndex,
                            String valueName) {
        super();
        this.keyIndex = keyIndex;
        this.keyName = keyName;
        this.valueIndex = valueIndex;
        this.valueName = valueName;
    }

    /**
     * This factory method is called by <code>handle()</code> to retrieve the
     * key value from the current <code>ResultSet</code> row.  This
     * implementation returns <code>ResultSet.getObject()</code> for the
     * configured key column name or index.
     *
     * @param rs ResultSet to create a key from
     * @return Object from the configured key column name/index
     * @throws SQLException       if a database access error occurs
     * @throws ClassCastException if the class datatype does not match the column type
     */
    // We assume that the user has picked the correct type to match the column
    // so getObject will return the appropriate type and the cast will succeed.
    @SuppressWarnings("unchecked")
    @Override
    protected K createKey(ResultSet rs) throws SQLException {
        return (keyName == null) ?
                (K) rs.getObject(keyIndex) :
                (K) rs.getObject(keyName);
    }

    /**
     * This factory method is called by <code>handle()</code> to store the
     * current <code>ResultSet</code> row in some object. This
     * implementation returns a <code>Map</code> with case insensitive column
     * names as keys.  Calls to <code>map.get("COL")</code> and
     * <code>map.get("col")</code> return the same value.
     *
     * @param rs ResultSet to create a row from
     * @return Object typed Map containing column names to values
     * @throws SQLException if a database access error occurs
     */
    @SuppressWarnings("unchecked")
    @Override
    protected V createRow(ResultSet rs) throws SQLException {
        return (valueName == null) ?
                (V) rs.getObject(valueIndex) :
                (V) rs.getObject(valueName);
    }

}
