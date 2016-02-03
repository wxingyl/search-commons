package com.tqmall.search.commons.nlp.trie;

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
    abstract void childHandle(NodeChildHandle<V> handle);

    public void clear() {
        value = null;
        status = null;
    }

    /**
     * 获取所有child的词
     *
     * @param prefixKey 前面已经匹配的key, 注意: 该参数已经包含该节点的字符
     * @return 对应的key已经Value
     */
    public abstract List<Map.Entry<String, V>> allChildWords(String prefixKey);

    final public Status getStatus() {
        return status;
    }

    final public V getValue() {
        return value;
    }

    final public void setValue(V value) {
        this.value = value;
    }

    final public char getChar() {
        return c;
    }

    final public boolean accept() {
        return status == Status.WORD || status == Status.LEAF_WORD;
    }

    /**
     * @param word       要删除的关键字, word不做空等的校验
     * @param startIndex 处理开始删除的节点
     * @return 是否中断删除
     */
    public boolean removeNode(char[] word, int startIndex) {
        int curChildPos = startIndex + 1;
        //最先检查是否已经删除了~~~
        if (status == Status.DELETE || curChildPos > word.length) return false;
        //到底了~~~
        if (curChildPos == word.length) {
            //说明该节点没有词, 要删除的key不对
            if (status == Status.NORMAL) return false;
        } else {
            Node<?> child = getChild(word[curChildPos]);
            if (child == null || !child.removeNode(word, curChildPos)) return false;
            if (status != Status.NORMAL) {
                //key正常, 只不过其上面的节点被其他词占用, 我们就不删除这些东东了~~~
                return false;
            }
        }

        //到这儿就说明可以删除了~~~
        value = null;
        status = haveChild() ? Status.NORMAL : Status.DELETE;
        return true;
    }


//  下面都是一些static方法定义了~~~~~~

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
        Status preStatus = preNode.status;
        switch (newNode.status) {
            case LEAF_WORD:
                if (preStatus == Status.NORMAL || preStatus == Status.DELETE) {
                    add = true;
                }
                if (preStatus != Status.WORD) {
                    preNode.status = Status.LEAF_WORD;
                }
                preNode.value = newNode.value;
                break;
            case NORMAL:
                if (preStatus == Status.LEAF_WORD) {
                    preNode.status = Status.WORD;
                } else if (preStatus == Status.DELETE) {
                    preNode.status = Status.NORMAL;
                }
                break;
            case DELETE:
            case WORD:
                throw new IllegalArgumentException("can not replace node which new status is " + Status.WORD
                        + " or " + Status.DELETE);
        }
        return add;
    }

    /**
     * 通过字符二分查找
     *
     * @param fromIndex the index of the first element (inclusive) to be
     *                  searched
     * @param toIndex   the index of the last element (exclusive) to be searched
     */
    public static int binarySearch(Node[] array, int fromIndex, int toIndex, char ch) {
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


    private static final TrieNodeFactory<?> DEFAULT_CJK_NODE_FACTORY = new TrieNodeFactory<Object>() {
        @Override
        public Node<Object> createRootNode() {
            return LargeRootNode.createCjkRootNode();
        }

        @Override
        public Node<Object> createNormalNode(char c) {
            return new NormalNode<>(c);
        }

        @Override
        public Node<Object> createChildNode(char c, Object value) {
            return new NormalNode<>(c, value);
        }
    };

    /**
     * 默认的前缀树cjk node factory
     *
     * @param <V> Node节点泛型
     * @return factory
     */
    @SuppressWarnings("unchecked")
    public static <V> TrieNodeFactory<V> defaultCjkTrieNodeFactory() {
        return (TrieNodeFactory<V>) DEFAULT_CJK_NODE_FACTORY;
    }

}
