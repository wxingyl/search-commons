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

import java.util.Map;

/**
 * 一个key对应多个值,返回一个 <K,Map<T,V>> 形式的值 map即数据库中的所有列,一行一个map,即一个bean
 *
 * @see org.apache.commons.dbutils.handlers.KeyedHandler
 */
public class FreeKeyedMapsListHandler<K> extends FreeKeyedMapsHandler<K> {

    public FreeKeyedMapsListHandler(int keyIndex, String keyName, RowProcessor rowProcessor) {
        super(keyIndex, keyName, rowProcessor);
    }

    @Override
    protected Map<K, Map<String, Object>> createMap() {
        return new MultiValueMap();
    }
}
