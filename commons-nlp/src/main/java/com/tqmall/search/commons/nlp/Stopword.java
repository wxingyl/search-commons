package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.lang.Function;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by xing on 16/3/8.
 * 停止词, 不区分大小写
 *
 * @author xing
 */
public class Stopword implements Iterable<String> {

    private final Set<String> stopwordSet;

    public Stopword() {
        stopwordSet = new HashSet<>();
        NlpUtils.loadLexicon(NlpConst.STOPWORD_FILE_NAME, new Function<String, Boolean>() {
            @Override
            public Boolean apply(String line) {
                stopwordSet.add(line);
                return true;
            }
        });
    }

    /**
     * 添加停止词
     *
     * @return 是否添加完成
     */
    public boolean addStopword(String word) {
        return stopwordSet.add(word.toLowerCase());
    }

    /**
     * 删除停止词
     *
     * @return 是否删除完成
     */
    public boolean removeStopword(String word) {
        return stopwordSet.remove(word);
    }

    @Override
    public Iterator<String> iterator() {
        return stopwordSet.iterator();
    }
}
