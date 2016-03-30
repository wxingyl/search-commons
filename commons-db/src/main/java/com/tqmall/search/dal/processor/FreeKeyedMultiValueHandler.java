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

import java.util.Map;

/**
 * 一个key对应多个值,返回一个 <K,List<V>> 形式的值
 *
 * @see org.apache.commons.dbutils.handlers.KeyedHandler
 */
public class FreeKeyedMultiValueHandler<K, V> extends FreeKeyedHandler<K, V> {

    public FreeKeyedMultiValueHandler(int keyIndex, String keyName, int valueIndex, String valueName) {
        super(keyIndex, keyName, valueIndex, valueName);
    }

    @SuppressWarnings("unchecked")
    protected Map<K, V> createMap() {
        return new MultiValueMap();
    }
}
