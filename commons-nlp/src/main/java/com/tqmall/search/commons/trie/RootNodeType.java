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
    NORMAL {
        @Override
        public <V> Node<V> createRootNode() {
            return new NormalNode<>('\0');
        }
    },
    ASCII {
        @Override
        public <V> Node<V> createRootNode() {
            return new BigRootNode<>(Character.MIN_VALUE, 0x100);
        }
    },
    CJK {
        @Override
        public <V> Node<V> createRootNode() {
            return new BigRootNode<>(NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST, NlpConst.CJK_UNIFIED_SIZE);
        }
    },
    ALL {
        @Override
        public <V> Node<V> createRootNode() {
            return new BigRootNode<>(Character.MIN_VALUE, 0x10000);
        }
    };

    public abstract <V> Node<V> createRootNode();

    public <V> TrieNodeFactory<V> defaultTrie() {
        return TrieNodeFactories.defaultTrie(this.<V>createRootNode());
    }

    public <V> AcTrieNodeFactory<V> defaultAcTrie() {
        return TrieNodeFactories.defaultAcTrie(this.<V>createRootNode());
    }

}
