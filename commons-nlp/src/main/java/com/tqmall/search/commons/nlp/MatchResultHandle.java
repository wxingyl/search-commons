package com.tqmall.search.commons.nlp;

/**
 * Created by xing on 16/2/3.
 * 匹配结果处理
 */
public class MatchResultHandle<V> implements Hit.IHit<V> {
    /**
     * 结果集
     */
    private final Hits<V> hits;
    /**
     * 匹配的源文本
     */
    private final String srcText;

    public MatchResultHandle(String text) {
        this.srcText = text;
        hits = new Hits<>();
    }

    /**
     * @param startPos 匹配到的开始位置, 包含startPos, 即左开右闭格式
     * @param endPos   匹配到的结束位置 不包含endPos, 即左开右闭格式
     * @param value    对应的value
     * @return 返回值已经记录了, 所以这儿不返回
     */
    @Override
    public Hit<V> onHit(int startPos, int endPos, V value) {
        hits.addHit(new Hit<>(endPos, srcText.substring(startPos, endPos), value));
        return null;
    }

    /**
     * 返回结果没有做不可修改的限制, 先这样吧
     *
     * @return 匹配结果
     */
    public Hits<V> getHits() {
        return hits;
    }

    public String getSrcText() {
        return srcText;
    }
}