package com.tqmall.search.commons.trie;

import java.util.List;
import java.util.Map;

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
     * 如果该节点为单独的词, 则该值有效
     */
    protected V value;
    /**
     * 当前节点的状态
     */
    protected Status status;

    public Node(char ch, Status status, V value) {
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
     * 是否存在有效的children
     */
    public abstract boolean haveChild();

    /**
     * 内部方法
     *
     * @param handle 处理接口
     */
    public abstract void childHandle(NodeChildHandle<V> handle);

    /**
     * @param word 要删除的关键字, word不做空的校验
     * @param deep 当前节点的深度, 根节点为0
     * @return 是否中断删除操作
     */
    public abstract boolean deleteNode(char[] word, final int deep);

    public abstract void clear();

    /**
     * 获取所有child的词
     * Note: root节点不支持该方法调用, 如果调用抛出{@link UnsupportedOperationException}
     *
     * @param prefixKey 前面已经匹配的key, 注意: 该参数已经包含该节点的字符
     * @return 对应的key已经Value
     * @see BigRootNode#allChildWords(char[])
     */
    public abstract List<Map.Entry<String, V>> allChildWords(char[] prefixKey);

    public final Status getStatus() {
        return status;
    }

    public final V getValue() {
        return value;
    }

    public final void setValue(V value) {
        this.value = value;
    }

    public final char getChar() {
        return c;
    }

    public final boolean accept() {
        return status == Status.WORD || status == Status.LEAF_WORD;
    }

    /**
     * 节点状态定义
     */
    public enum Status {
        //删除的节点
        DELETE,
        //普通的,不是词的节点
        NORMAL,
        //词的结尾, 但不是叶子节点
        WORD,
        //叶子节点
        LEAF_WORD
    }

    /**
     * 节点替换操作, 将原先的节点挺欢成新加的节点
     * newNode的status为叶子节点, 将newNode的value赋值给preNode, 但是只有在preNode为null时, 才认为是新添加的节点, 返回值为true
     *
     * @param preNode 子节点上原先的节点
     * @param newNode 子节点上新加的节点
     * @param <V>     value 的泛型
     * @return 是否这行了添加操作
     */
    protected static <V> boolean handleReplaceChildNode(Node<V> preNode, Node<V> newNode) {
        Status preStatus = preNode.status;
        if (newNode.status == Status.LEAF_WORD) {
            boolean add = preStatus == Status.NORMAL || preStatus == Status.DELETE;
            if (preStatus != Status.LEAF_WORD) {
                preNode.status = Status.WORD;
            }
            preNode.value = newNode.value;
            return add;
        } else if (newNode.status == Status.NORMAL) {
            if (preStatus == Status.LEAF_WORD) {
                preNode.status = Status.WORD;
            } else if (preStatus == Status.DELETE) {
                preNode.status = Status.NORMAL;
            }
            return false;
        } else {
            throw new IllegalArgumentException("can not replace node which new status is " + Status.WORD
                    + " or " + Status.DELETE);
        }
    }

    /**
     * 通过字符二分查找
     *
     * @param fromIndex the index of the first element (inclusive) to be
     *                  searched
     * @param toIndex   the index of the last element (exclusive) to be searched
     */
    public static int binarySearch(final Node[] array, final int fromIndex, final int toIndex, final char ch) {
        int low = fromIndex, high = toIndex - 1;
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
        return ~low;  // key not found.
    }

}
