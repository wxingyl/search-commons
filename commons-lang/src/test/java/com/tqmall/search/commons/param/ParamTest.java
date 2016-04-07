package com.tqmall.search.commons.param;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by xing on 16/4/2.
 *
 * @author xing
 */
public class ParamTest {

    @Test
    public void sourceNameTest() {
        Assert.assertEquals(SourceNameFactoryImpl.INSTANCE.sourceName(), RpcParams.SOURCE);
    }

}
