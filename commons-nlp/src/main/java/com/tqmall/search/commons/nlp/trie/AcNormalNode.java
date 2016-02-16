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
        for (int i = 0; i < childCount; i++) {
            @SuppressWarnings({"rawtypes","unchecked"})
            AcNormalNode<V> acNode = (AcNormalNode<V>) children[i];
            acNode.innerBuildFailed(root);
        }
    }

    private void innerBuildFailed(final Node<V> root) {
        AcNormalNode<V> lParent = parent;
        while (failed == null) {
            if (lParent.failed == null) {
                lParent.innerBuildFailed(root);
            }
            Node<V> node = lParent.failed.getChild(c);
            if (node != null) {
                failed = node;
            } else if (lParent.failed == root) {
                failed = root;
            } else {
                lParent = (AcNormalNode<V>) lParent.failed;
            }
        }
        for (int i = 0; i < childCount; i++) {
            @SuppressWarnings({"rawtypes","unchecked"})
            AcNormalNode<V> acNode = (AcNormalNode<V>) children[i];
            acNode.innerBuildFailed(root);
        }
    }

    private static final AcTrieNodeFactory<?> DEFAULT_CJK_AC_NODE_FACTORY = new AcTrieNodeFactory<Object>() {
        @Override
        public Node<Object> createRootNode() {
            return LargeRootNode.createCjkRootNode();
        }

        @Override
        public AcNormalNode<Object> createNormalNode(char c) {
            return new AcNormalNode<>(c);
        }

        @Override
        public AcNormalNode<Object> createChildNode(char c, Object value) {
            return new AcNormalNode<>(c, value);
        }
    };

    /**
     * 默认的前缀树Cjk AcNode Factory
     *
     * @param <V> Node节点泛型
     * @return factory
     */
    @SuppressWarnings("unchecked")
    public static <V> AcTrieNodeFactory<V> defaultCjkAcTrieNodeFactory() {
        return (AcTrieNodeFactory<V>) DEFAULT_CJK_AC_NODE_FACTORY;
    }
}
