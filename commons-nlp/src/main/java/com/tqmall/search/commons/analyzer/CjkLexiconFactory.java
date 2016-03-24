package com.tqmall.search.commons.analyzer;

import com.tqmall.search.commons.lang.Supplier;
import com.tqmall.search.commons.trie.RootNodeType;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Created by xing on 16/3/22.
 * CjkLexicon 的{@link Supplier}实现
 * 加载词库实现异步加载, 实现{@link CjkLexicon}对应够高参数单例
 *
 * @author xing
 */
public class CjkLexiconFactory implements Supplier<CjkLexicon> {

    private final Object lock = new Object();

    private CjkLexicon cjkLexicon;

    private boolean isNull = true;

    CjkLexiconFactory(final RootNodeType rootType, final Collection<Path> lexiconPaths) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    cjkLexicon = new CjkLexicon(rootType == null ? RootNodeType.CJK : rootType,
                            lexiconPaths);
                    isNull = false;
                    lock.notifyAll();
                }
            }
        }, "benz-load-lexicon");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public CjkLexicon get() {
        if (isNull) {
            synchronized (lock) {
                while (isNull) {
                    try {
                        //顶多等100ms
                        lock.wait(100L);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
        return cjkLexicon;
    }

    public static CjkLexiconFactory valueOf(RootNodeType rootType, Collection<Path> lexiconPaths) {
        return new CjkLexiconFactory(rootType, lexiconPaths);
    }
}
