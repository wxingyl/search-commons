package com.tqmall.search.commons.utils;

/**
 * Created by xing on 16/3/18.
 *
 * @author xing
 */
@SuppressWarnings({"rawtypes, unchecked"})
public enum HttpMethod {
    GET {
        @Override
        public <T extends HttpUtils.Request> T build() {
            return (T) new HttpUtils.GetRequest();
        }
    },
    POST {
        @Override
        public <T extends HttpUtils.Request> T build() {
            return (T) new HttpUtils.PostRequest();
        }
    },
    PUT {
        @Override
        public <T extends HttpUtils.Request> T build() {
            return (T) new HttpUtils.PutRequest();
        }
    },
    DELETE {
        @Override
        public <T extends HttpUtils.Request> T build() {
            return (T) new HttpUtils.DeleteRequest();
        }
    };

    public abstract <T extends HttpUtils.Request> T build();

}
