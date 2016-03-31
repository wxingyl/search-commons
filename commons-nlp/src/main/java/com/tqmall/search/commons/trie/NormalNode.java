package com.tqmall.search.commons.trie;

import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/1/27.
 * 普通的node节点
 */
public class NormalNode<V> extends Node<V> {

    /**
     * 默认数组扩展大小
     */
    protected static final int DEFAULT_INFLATE_SIZE = 8;

    protected int childCount;

    protected Node<?>[] children;

    /**
     * 普通节点构造
     *
     * @param ch 对应字符
     */
    public NormalNode(char ch) {
        this(ch, Status.NORMAL, null);
    }

    /**
     * 叶子节点构造
     *
     * @param ch    对应字符
     * @param value 叶子节点对应的值
     */
    public NormalNode(char ch, V value) {
        this(ch, Status.LEAF_WORD, value);
    }

    public NormalNode(char ch, Status status, V value) {
        super(ch, status, value);
    }

    /**
     * 扩展children数组, 每次扩展DEFAULT_INFLATE_SIZE
     */
    private void inflateChildrenArray() {
        if (children == null) {
            children = new Node[DEFAULT_INFLATE_SIZE];
        } else {
            Node[] newChildren = new Node[children.length + DEFAULT_INFLATE_SIZE];
            System.arraycopy(children, 0, newChildren, 0, children.length);
            children = newChildren;
        }
    }

    @SuppressWarnings({"rawstype", "unchecked"})
    @Override
    public boolean addChild(Node<V> node) {
        //first child, simple handle
        if (children == null) {
            inflateChildrenArray();
            children[0] = node;
            childCount++;
            return true;
        }
        int index = binarySearch(children, 0, childCount, node.c);
        if (index < 0) {
            index = -(index + 1);
            if ((childCount + 1) >= children.length) {
                inflateChildrenArray();
            }
            if (index < childCount) {
                System.arraycopy(children, index, children, index + 1, childCount - index);
            }
            children[index] = node;
            childCount++;
            return true;
        } else {
            //说明原先已经存在了, 替换吧
            return handleReplaceChildNode((Node<V>) children[index], node);
        }
    }

    @SuppressWarnings({"rawstype", "unchecked"})
    @Override
    public Node<V> getChild(char ch) {
        if (children == null) return null;
        int index = binarySearch(children, 0, childCount, ch);
        return index < 0 ? null : (Node<V>) children[index];
    }

    /**
     * 普通节点删除
     *
     * @return 是否中断删除操作
     */
    @Override
    public boolean deleteNode(char[] word, final int deep) {
        //最先检查是否已经删除了~~~
        if (status == Status.DELETE || deep > word.length) return true;
        //到底了~~~
        if (deep == word.length) {
            //说明该节点没有词, 要删除的key不对, 这儿一般不会出现, 只是做一个安全校验
            if (status == Status.NORMAL) return true;
        } else {
            Node<?> child = getChild(word[deep]);
            if (child == null || child.deleteNode(word, deep + 1)) return true;
            if (status != Status.NORMAL) {
                //key正常, 只不过其上面的节点被其他词占用, 此时需要停止删除操作
                return true;
            }
        }

        //到这儿就说明可以删除了~~~
        value = null;
        status = haveChild() ? Status.NORMAL : Status.DELETE;
        return false;
    }

    @Override
    public boolean haveChild() {
        if (children == null) return false;
        for (int i = 0; i < childCount; i++) {
            if (children[i].status != Status.DELETE) return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings({"rawstype", "unchecked"})
    public void childHandle(NodeChildHandle<V> handle) {
        if (children == null) return;
        for (int i = 0; i < childCount; i++) {
            if (children[i].status != Status.DELETE) {
                if (!handle.onHandle((Node<V>) children[i])) break;
            }
        }
    }

    @SuppressWarnings({"rawstype", "unchecked"})
    private void walkAppend(StringBuilder preKey, List<Map.Entry<String, V>> retList) {
        if (status == Status.DELETE) return;
        preKey.append(c);
        if (accept()) {
            retList.add(CommonsUtils.newImmutableMapEntry(preKey.toString(), value));
        }
        if (children != null) {
            final int startIndex = preKey.length();
            for (int i = 0; i < childCount; i++) {
                NormalNode<V> childNode = (NormalNode<V>) children[i];
                childNode.walkAppend(preKey, retList);
                preKey.delete(startIndex, preKey.length());
            }
        }
    }

    @Override
    public List<Map.Entry<String, V>> allChildWords(char[] prefixKey) {
        if (prefixKey == null || prefixKey.length == 0) return null;
        if (prefixKey[prefixKey.length - 1] != c) {
            throw new IllegalArgumentException("the prefixKey: " + String.valueOf(prefixKey) + " last char isn't " + c);
        }
        List<Map.Entry<String, V>> retList = new ArrayList<>();
        walkAppend(new StringBuilder().append(prefixKey, 0, prefixKey.length - 1), retList);
        return retList;
    }

    @Override
    public void clear() {
        value = null;
        status = Status.DELETE;
        for (int i = 0; i < childCount; i++) {
            children[i].clear();
            children[i] = null;
        }
        childCount = 0;
        children = null;
    }

    private final static int HASH_CODE_FACTOR = NormalNode.class.getSimpleName().hashCode();

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + HASH_CODE_FACTOR;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NormalNode && super.equals(o);
    }
}
