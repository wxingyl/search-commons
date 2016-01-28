package com.tqmall.search.commons.nlp.trie;

import java.util.List;
import java.util.Map;

import static com.tqmall.search.commons.nlp.trie.NodeStatus.LEAF_WORD;
import static com.tqmall.search.commons.nlp.trie.NodeStatus.WORD;

/**
 * Created by xing on 16/1/27.
 * trie树节点对象
 */
public abstract class Node<V> {

    /**
     * 当前字符
     */
    protected final char c;
    /**
     * 子节点
     */
    protected Node<?>[] children;
    /**
     * 如果该节点为单独的词, 则该值有效
     */
    protected V value;
    /**
     * 当前节点的状态
     */
    protected NodeStatus status;

    public Node(char ch, NodeStatus status, V value) {
        this.c = ch;
        this.status = status;
        this.value = value;
    }

    /**
     * 添加叶子节点
     *
     * @return true表示完成添加, false表示原先已经存在并以覆盖
     */
    public abstract boolean addChild(Node<V> node);

    public abstract Node<V> getChild(char ch);

    /**
     * 获取所有child的词
     *
     * @param prefixKey 前面已经匹配的key, 注意: 该参数已经包含该节点的字符
     * @return 对应的key已经Value
     */
    public abstract List<Map.Entry<String, V>> allChildWords(String prefixKey);

    public NodeStatus getStatus() {
        return status;
    }

    final public V getValue() {
        return value;
    }

    final public char getChar() {
        return c;
    }

    /**
     * newNode的status为叶子节点, 讲newNode的value赋值给preNode, 但是只有在preNode为null时, 才认为是新添加的节点, 返回值为true
     * 添加进来的newNode只有两个状态: 普通的status = 0 的节点
     *
     * @param preNode 子节点上原先的节点
     * @param newNode 子节点上新加的节点
     * @param <V>     value 的泛型
     * @return 是否这行了添加操作
     */
    protected static <V> boolean handleReplaceChildNode(Node<V> preNode, Node<V> newNode) {
        boolean add = false;
        switch (newNode.status) {
            case LEAF_WORD:
                if (preNode.status != NodeStatus.WORD) {
                    preNode.status = LEAF_WORD;
                }
                if (preNode.value != null) {
                    add = true;
                }
                preNode.value = newNode.value;
                break;
            case NORMAL:
                if (preNode.status == LEAF_WORD) {
                    preNode.status = NodeStatus.WORD;
                }
                break;
            case WORD:
                throw new IllegalArgumentException("can not replace node which new status is " + WORD);
        }
        return add;
    }

    /**
     * 通过字符二分查找
     */
    public static int binarySearch(Node[] array, char ch) {
        int low = 0, high = array.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            Node midVal = array[mid];
            if (midVal.c > ch)
                low = mid + 1;
            else if (midVal.c < ch)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }
}
