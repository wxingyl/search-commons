package com.tqmall.search.commons.nlp.node;

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
}
