package com.tqmall.search.commons.nlp;

import org.junit.Ignore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/2/1.
 * 拼音test
 */
public class PyTest {

    @Ignore
    public void allSimpleTest() {
        List<Character> simpleList = new ArrayList<>();
        for (char ch = NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST; ch <= NlpConst.CJK_UNIFIED_IDEOGRAPHS_LAST; ch++) {
            if (!NlpUtils.isTraditional(ch)) {
                simpleList.add(ch);
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/xing/build/nlp/simple.txt"))) {
            for (Character ch : simpleList) {
                writer.write(ch.toString());
                writer.newLine();
            }
            writer.flush();
            System.out.println("write size: " + simpleList.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
