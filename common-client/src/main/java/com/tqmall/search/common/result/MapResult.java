package com.tqmall.search.common.result;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xing on 15/12/6.
 * Result data结构为Map<String, Object>
 */
public class MapResult extends Result<Map<String, Object>> implements Serializable {

    private static final long serialVersionUID = 1530948710568686057L;

    public MapResult() {
        super(new HashMap<String, Object>());
    }

    public MapResult(Map<String, Object> data) {
        super(data);
    }

    public MapResult(ErrorCode errorCode) {
        super(errorCode);
    }

    public void put(String key, Object val) {
        getData().put(key, val);
    }

}
