package com.tqmall.search.commons.trie;

import com.tqmall.search.commons.ac.AcTrieNodeFactory;
import com.tqmall.search.commons.nlp.NlpConst;

/**
 * Created by xing on 16/3/11.
 * {@link TrieNodeFactory}, {@link AcTrieNodeFactory}工具类
 *
 * @author xing
 * @see TrieNodeFactory
 * @see AcTrieNodeFactory
 */
public enum RootNodeType {
    NORMAL,
    ASCII,
    CJK,
    ALL;

    @SuppressWarnings("unchecked")
    public <V> Node<V> createRootNode() {
        Node<V> root;
        switch (this) {
            case NORMAL:
                root = new NormalNode('\0');
                break;
            case ASCII:
                root = new BigRootNode<>(Character.MIN_VALUE, 0x100);
                break;
            case CJK:
                root = new BigRootNode<>(NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST, NlpConst.CJK_UNIFIED_SIZE);
                break;
            case ALL:
                root = new BigRootNode<>(Character.MIN_VALUE, 0x10000);
                break;
            default:
                //can not arrive here
                root = null;
        }
        return root;
    }

    public <V> TrieNodeFactory<V> defaultTrie() {
        return TrieNodeFactories.defaultTrie(this.<V>createRootNode());
    }

    public <V> AcTrieNodeFactory<V> defaultAcTrie() {
        return TrieNodeFactories.defaultAcTrie(this.<V>createRootNode());
    }

}
