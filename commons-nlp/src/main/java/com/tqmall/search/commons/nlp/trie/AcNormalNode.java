package com.tqmall.search.commons.nlp.trie;

/**
 * Created by xing on 16/1/28.
 * Aho-Corasick 多模式匹配算法实现, AC自动机节点
 * 这个博客讲的不错: http://www.cnblogs.com/xudong-bupt/p/3433506.html
 */
public class AcNormalNode<V> extends NormalNode<V> {

    /**
     * 失败节点
     */
    private Node failed;

    /**
     * 父节点, 如果深度为1的节点, 则父节点为null
     */
    private AcNormalNode parent;

    /**
     * 普通节点构造
     *
     * @param ch 对应字符
     */
    public AcNormalNode(char ch) {
        super(ch);
    }


    /**
     * 叶子节点构造
     *
     * @param ch    对应字符
     * @param value 叶子节点对应的值
     */
    public AcNormalNode(char ch, V value) {
        super(ch, value);
    }

    public AcNormalNode(char ch, Status status, V value) {
        super(ch, status, value);
    }

    @Override
    public boolean removeNode(char[] word, int startIndex) {
        throw new UnsupportedOperationException("AcNode can't support remove");
    }

    public Node getFailed() {
        return failed;
    }

    /**
     * 只对深度为1的节点进行单独的初始化
     *
     * @param root 根节点对象
     */
    void initRootChildNode(final Node root) {
        //深度为1的节点需要单独设定
        this.failed = root;
        initChildParent();
    }

    /**
     * 初始化节点parent
     */
    private void initChildParent() {
        if (children != null) {
            for (int i = 0; i < childCount; i++) {
                AcNormalNode acNode = (AcNormalNode) children[i];
                acNode.parent = this;
                acNode.initChildParent();
            }
        }
    }

    /**
     * 初始化failed字段
     * 深度为1的节点需要单独设定
     *
     * @param root   root根节点
     */
    void buildFailed(final Node root) {
        if (parent != null) {
            AcNormalNode lParent = parent;
            while (failed == null) {
                Node node = lParent.failed.getChild(c);
                if (node != null) {
                    failed = node;
                } else if (lParent.failed == root) {
                    failed = root;
                } else {
                    lParent = (AcNormalNode) lParent.failed;
                }
            }
        }
        if (children == null) return;
        for (int i = 0; i < childCount; i++) {
            AcNormalNode acNode = (AcNormalNode) children[i];
            acNode.buildFailed(root);
        }
    }
}
