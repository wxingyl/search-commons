package com.tqmall.search.commons.ac;

import com.tqmall.search.commons.trie.Node;
import com.tqmall.search.commons.trie.NormalNode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Created by xing on 16/1/28.
 * Aho-Corasick 多模式匹配算法实现, AC自动机节点
 * 这个博客讲的不错: http://www.cnblogs.com/xudong-bupt/p/3433506.html
 */
public class AcNormalNode<V> extends NormalNode<V> {

    /**
     * 失败节点
     */
    private Node<V> failed;

    /**
     * 父节点, 如果深度为1的节点, 则父节点为null
     */
    private AcNormalNode<V> parent;
    /**
     * 输出
     */
    private String singleOutput;

    /**
     * 普通节点构造
     *
     * @param ch 对应字符
     */
    public AcNormalNode(char ch) {
        this(ch, Status.NORMAL, null);
    }


    /**
     * 叶子节点构造
     *
     * @param ch    对应字符
     * @param value 叶子节点对应的值
     */
    public AcNormalNode(char ch, V value) {
        this(ch, Status.LEAF_WORD, value);
    }

    public AcNormalNode(char ch, Status status, V value) {
        super(ch, status, value);
    }

    @Override
    public boolean removeNode(char[] word, int startIndex) {
        throw new UnsupportedOperationException("AcNode can't support remove");
    }

    public Node<V> getFailed() {
        return failed;
    }

    /**
     * 只对深度为1的节点进行单独的初始化
     *
     * @param root 根节点对象
     */
    void initRootChildNode(final Node<V> root) {
        //深度为1的节点需要单独设定
        this.failed = root;
        initChildParent(new StringBuilder().append(c));
    }

    /**
     * 初始化节点parent
     *
     * @param outputSb 当前节点的字符c已经添加
     */
    private void initChildParent(StringBuilder outputSb) {
        if (accept()) {
            this.singleOutput = outputSb.toString();
        }
        if (children != null) {
            for (int i = 0; i < childCount; i++) {
                AcNormalNode acNode = (AcNormalNode) children[i];
                acNode.parent = this;
                acNode.failed = null;
                outputSb.append(acNode.c);
                acNode.initChildParent(outputSb);
                outputSb.deleteCharAt(outputSb.length() - 1);
            }
        }
    }

    public String getSingleOutput() {
        return singleOutput;
    }

    @Override
    public void clear() {
        super.clear();
        failed = null;
        parent = null;
        singleOutput = null;
    }

    /**
     * 初始化failed字段
     * 深度为1的节点需要单独设定
     *
     * @param root root根节点
     */
    void buildFailed(final Node<V> root) {
        if (parent != null) {
            throw new IllegalArgumentException("current node depth is not 1");
        }
        /**
         * 这儿把Deque当成Stack用
         */
        Deque<AcNormalNode<V>> stack = new LinkedList<>();
        this.addChildToStack(stack);
        AcNormalNode<V> lParent = null;
        while (!stack.isEmpty()) {
            AcNormalNode<V> curNode = stack.getFirst();
            if (curNode.failed == null) {
                if (lParent == null) {
                    lParent = curNode.parent;
                }
                if (lParent.failed == null) {
                    stack.push(lParent);
                    lParent = null;
                    continue;
                }
                Node<V> curFailedNode = lParent.failed.getChild(curNode.c);
                if (curFailedNode != null) {
                    //找到失败节点
                    curNode.failed = curFailedNode;
                } else if (lParent.failed == root) {
                    //没有找到, 但父节点的failed就是root, 就没有必要继续找下去了
                    curNode.failed = root;
                } else {
                    //没有找到, 但父节点的failed节点有效,
                    lParent = (AcNormalNode<V>) lParent.failed;
                }
            }
            if (curNode.failed != null) {
                //当前节点的failed节点初始化完成
                stack.pollFirst();
                lParent = null;
                curNode.addChildToStack(stack);
            }
        }
    }

    /**
     * 讲children添加到队列开始位置, 即入栈
     */
    private void addChildToStack(Deque<AcNormalNode<V>> deque) {
        for (int i = 0; i < childCount; i++) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            AcNormalNode<V> acNode = (AcNormalNode<V>) children[i];
            deque.push(acNode);
        }
    }

}
