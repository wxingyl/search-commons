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

import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.AbstractKeyedHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * 一个key对应多个值,返回一个 <K,Map<T,V>> 形式的值 map即数据库中的所有列,一行一个map,即一个bean
 *
 * @see org.apache.commons.dbutils.handlers.KeyedHandler
 */
public class FreeKeyedMapsHandler<K> extends AbstractKeyedHandler<K, Map<String, Object>> {

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
     * The RowProcessor implementation to use when converting rows into Objects.
     */
    private final RowProcessor convert;

    public FreeKeyedMapsHandler(int keyIndex,
                                String keyName, RowProcessor rowProcessor) {
        this.keyIndex = keyIndex;
        this.keyName = keyName;
        this.convert = rowProcessor;
    }

    @Override
    protected K createKey(ResultSet rs) throws SQLException {
        return (keyName == null) ?
                (K) rs.getObject(keyIndex) :
                (K) rs.getObject(keyName);
    }

    @Override
    protected Map<String, Object> createRow(ResultSet rs) throws SQLException {
        return convert.toMap(rs);
    }
}
