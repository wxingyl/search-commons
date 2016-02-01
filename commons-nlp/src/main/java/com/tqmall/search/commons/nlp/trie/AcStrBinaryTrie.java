package com.tqmall.search.commons.nlp.trie;

import java.util.Collection;
import java.util.List;

/**
 * Created by xing on 16/2/1.
 * 没有value, 只有key的{@link AcBinaryTrie}, 该类实时为了方便使用
 *
 * @see AcBinaryTrie
 */
public class AcStrBinaryTrie implements AcTrie<Void> {

    private AcBinaryTrie<Void> acBinaryTrie;

    public AcStrBinaryTrie(AcBinaryTrie<Void> acBinaryTrie) {
        this.acBinaryTrie = acBinaryTrie;
    }

    @Override
    public Void getValue(String key) {
        return null;
    }

    @Override
    public boolean updateValue(String key, Void value) {
        return false;
    }

    @Override
    public List<Hit<Void>> parseText(String text) {
        return acBinaryTrie.parseText(text);
    }

    @Override
    public int size() {
        return acBinaryTrie.size();
    }

    @Override
    public void clear() {
        acBinaryTrie.clear();
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {

        private AcBinaryTrie.Builder<Void> builder = AcBinaryTrie.build();

        public Builder nodeFactory(AcTrieNodeFactory<Void> nodeFactory) {
            builder.nodeFactory(nodeFactory);
            return this;
        }

        public Builder add(String key) {
            builder.put(key, null);
            return this;
        }

        public Builder add(String... keys) {
            for (String key : keys) {
                builder.put(key, null);
            }
            return this;
        }

        public Builder addAll(Collection<? extends String> keys) {
            for (String key : keys) {
                builder.put(key, null);
            }
            return this;
        }

        public AcStrBinaryTrie create() {
            return new AcStrBinaryTrie(builder.create());
        }
    }
}
