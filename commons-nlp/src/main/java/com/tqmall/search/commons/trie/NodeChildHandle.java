package com.tqmall.search.commons.trie;

/**
 * Created by xing on 16/1/31.
 * 处理节点的child
 */
public interface NodeChildHandle<V> {

    /**
     * @param child 该节点对应的child
     * @return 是否继续, false表示终止处理
     */
    boolean onHandle(Node<V> child);
}
