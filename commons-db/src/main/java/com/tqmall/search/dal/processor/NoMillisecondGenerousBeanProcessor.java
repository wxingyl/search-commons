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


import org.apache.commons.dbutils.GenerousBeanProcessor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 这个类返回的值是不带毫秒的，如果要带毫秒,请直接使用{@link GenerousBeanProcessor}
 */
public class NoMillisecondGenerousBeanProcessor extends GenerousBeanProcessor {

    private final String DATE_FORMAT_LONG = "yyyy-MM-dd HH:mm:ss";

    private final String DATE_FORMAT_SHORT = "yyyy-MM-dd";

    /**
     * Default constructor.
     */
    public NoMillisecondGenerousBeanProcessor() {
        super();
    }

    @Override
    protected Object processColumn(ResultSet rs, int index, Class<?> propType) throws SQLException {
        Object obj = super.processColumn(rs, index, propType);

        if (propType.equals(String.class) && obj != null && isDate((String) obj)) {
            obj = formatDate((String) obj);//TODO 此处待优化
        }
        return obj;
    }

    /**
     * yyyy-MM-dd HH:mm:ss
     *
     * @param str
     * @return
     */
    private Date toDateTime(String str) {
        Date d = null;
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_LONG);
        try {
            d = df.parse(str);
        } catch (Exception e) {
        }
        return d;
    }

    private boolean isDate(String str) {
        try {
            new SimpleDateFormat(DATE_FORMAT_SHORT).parse(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean isDateOnly(String str) {
        if (isDate(str)) {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_LONG);
            try {
                df.parse(str);
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    private String formatDate(String str) {
        if (isDateOnly(str)) {
            return formatDate(str, true);
        }
        return formatDate(str, false);
    }

    private String formatDate(String str, boolean onlyDate) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_LONG);
        if (onlyDate) {
            df = new SimpleDateFormat(DATE_FORMAT_SHORT);
        }
        Date date = null;
        try {
            date = df.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return df.format(date);
    }

}
