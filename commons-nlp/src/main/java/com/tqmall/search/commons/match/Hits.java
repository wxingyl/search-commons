package com.tqmall.search.commons.match;

import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.*;

/**
 * Created by xing on 16/3/21.
 * 匹配结果输出带有匹配到的字符串对象
 *
 * @author xing
 */
public class Hits<V> implements Iterable<Hits.InHit<V>> {

    private final List<InHit<V>> hits;

    public Hits(char[] text, List<Hit<V>> hits) {
        if (CommonsUtils.isEmpty(hits)) this.hits = Collections.emptyList();
        else {
            List<InHit<V>> inHits = new ArrayList<>(hits.size());
            for (Hit<V> hit : hits) {
                inHits.add(new InHit<>(hit, String.valueOf(text, hit.getStart(), hit.length())));
            }
            this.hits = Collections.unmodifiableList(inHits);
        }
    }

    /**
     * @return List is {@link Collections#unmodifiableList(List)}
     * @see Collections#unmodifiableList(List)
     */
    public List<InHit<V>> getHits() {
        return hits;
    }

    @Override
    public Iterator<InHit<V>> iterator() {
        return hits.iterator();
    }

    public String toString() {
        return hits.toString();
    }

    public static <V> Hits<V> valueOf(String text, List<Hit<V>> hits) {
        return new Hits<>(text.toCharArray(), hits);
    }

    public static <V> Hits<V> valueOf(char[] text, List<Hit<V>> hits) {
        return new Hits<>(text, hits);
    }

    public static class InHit<V> {

        private final Hit<V> hit;

        private final String key;

        public InHit(Hit<V> hit, String key) {
            this.hit = hit;
            this.key = key;
        }

        public Hit<V> getHit() {
            return hit;
        }

        public String getKey() {
            return key;
        }

        public String toString() {
            return key + '[' + hit.getStart() + ',' + hit.getEnd() + ']'
                    + (hit.getValue() == null ? "" : hit.getValue().toString());
        }
    }
}
