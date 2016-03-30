package org.lionsoul.jcseg;

import org.lionsoul.jcseg.core.ADictionary;
import org.lionsoul.jcseg.core.IChunk;
import org.lionsoul.jcseg.core.IWord;
import org.lionsoul.jcseg.core.JcsegTaskConfig;

import java.io.*;
import java.util.*;


/**
 * simplex segment for JCSeg,
 * has extend from ASegment. <br />
 *
 * @author yibo.liu<yibo.liu@tqmall.com>
 */
public class SimpleShortSeg extends ASegment {

    public SimpleShortSeg(JcsegTaskConfig config, ADictionary dic) throws IOException {
        super(config, dic);
    }

    public SimpleShortSeg(Reader input,
                          JcsegTaskConfig config, ADictionary dic) throws IOException {
        super(input, config, dic);
    }

    private String input;

    /**
     * stream/reader reset.
     * @throws java.io.IOException
     */
    public void reset(String input) throws IOException {
        this.input = input;
        super.reset(new StringReader(input));
    }

    private List<IWord> iWords = new ArrayList<>();
    private Set<String> iWordSet = new HashSet<>();

    public IWord[] nextAll() throws IOException {
        iWords = new ArrayList<>();

        input = "艹" + input;// input.substring(1); 第一次时，相当于减1了，即第一次从0开始
        while (input.length() > 2) {
            //循环执行多次，每一次去掉最前一个字符。
            //用于把 ABCDE 中的 AB、BC、CD类似的词取出
            input = input.substring(1);
            reset(input);
            nextAllInner();
        }
        if (!iWords.isEmpty()) {
            return iWords.toArray(new IWord[iWords.size()]);
        } else {
            return null;
        }
    }

    private void nextAllInner() throws IOException {
        int before = iWords.size();
        while (true) {
            IWord iWord = super.next();
            if (iWord == null) {//如果为空，则到了最后一个
                if (iWords.size() == before) {
                    //如果之前的值和现在的值一样多，则说明没有增加元素，退出。
                    // 否则再执行一次，用于把ABCDE中和AB、ABC、ABCD都能取出来
                    break;
                }
                before = iWords.size();
                reset(input);
                continue;
            }
            if (iWordSet.add(iWord.getValue())) {
                iWords.add(iWord);
            }
        }

    }

    /**
     * @see ASegment#getBestCJKChunk(char[], int)
     */
    @Override
    public IChunk getBestCJKChunk(char[] chars, int index) {
        IWord[] words = getNextMatch(chars, index);

        int i = 0;
        IWord iWord = words[i];
        while (iWordSet.contains(iWord.getValue())) {
            if (words.length == i) {//如果到最后一个了则退出
                break;
            }
            iWord = words[i++];
        }
        return new Chunk(new IWord[]{iWord});
    }

}
