package com.tqmall.search.commons.param;

/**
 * Created by xing on 16/4/2.
 *
 * @author xing
 */
public class SourceNameFactoryImpl implements SourceNameFactory {

    static final SourceNameFactory INSTANCE = new SourceNameFactoryImpl();

    @Override
    public String sourceName() {
        return "search-commons";
    }
}
