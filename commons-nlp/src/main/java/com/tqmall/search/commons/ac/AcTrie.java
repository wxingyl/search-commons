package com.tqmall.search.commons.ac;

import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.trie.Trie;

import java.util.List;

/**
 * Created by xing on 16/1/28.
 * Aho-Corasick 模式匹配树 接口定义
 * 接收态的值即为泛型V
 */
public interface AcTrie<V> extends Trie<V> {
    /**
     * 该接口修改Trie树中的节点结构, 如果要生效, 必须从新{@link #initFailed()}
     *
     * @see #initFailed()
     */
    boolean put(String key, V value);

    /**
     * 该接口修改Trie树中的节点结构, 如果要生效, 必须从新{@link #initFailed()}
     *
     * @see #initFailed()
     */
    boolean remove(String key);

    /**
     * 更新key对应的value, 如果key不存在直接返回false, 不会主动创建依赖节点
     *
     * @return 是否成功执行了更新, 如果找到了合适的节点, 则更新成功[value是否与原先的有变化不做判断]
     */
    boolean updateValue(String key, V value);

    /**
     * 构建failed字段
     */
    void initFailed();

    /**
     * 匹配字符串, 未匹配的字符串不做任何处理, 同{@link #match(char[], int, int)}
     *
     * @param text 需要匹配的文本
     * @return 匹配结果
     */
    List<Hit<V>> match(String text);

    /**
     * 匹配字符串, 未匹配的字符串不做任何处理
     *
     * @param text     需要匹配的文本
     * @param startPos 开始下标
     * @param length   char数组的长度
     * @return 匹配结果
     */
    List<Hit<V>> match(char[] text, int startPos, int length);
}
