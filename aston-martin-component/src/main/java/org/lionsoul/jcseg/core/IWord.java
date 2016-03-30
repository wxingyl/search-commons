package org.lionsoul.jcseg.core;

/**
 * Word interface
 * 
 * @author	chenxin<chenxin619315@gmail.com>
 */
public interface IWord {
	
	String[] NAME_POSPEECH = {"nr"};
	String[] NUMERIC_POSPEECH = {"m"};
	String[] EN_POSPEECH = {"en"};
	String[] MIX_POSPEECH = {"mix"};
	String[] PPT_POSPEECH = {"nz"};
	String[] PUNCTUATION = {"w"};
	String[] UNRECOGNIZE = {"urg"};
			
	/**
	 * China,JPanese,Korean words 
	 */
	int T_CJK_WORD = 1;
	
	/**
	 * chinese and english mix word.
	 * 		like B超,SIM卡. 
	 */
	int T_MIXED_WORD = 2;
	
	/**
	 * chinese last name. 
	 */
	int T_CN_NAME = 3;
	
	/**
	 * chinese nickname.
	 * like: 老陈 
	 */
	int T_CN_NICKNAME = 4;
	
	/**
	 * latain series.
	 * 	including the arabic numbers.
	 */
	int T_BASIC_LATIN = 5;
	
	/**
	 * letter number like 'ⅠⅡ' 
	 */
	int T_LETTER_NUMBER = 6;
	
	/**
	 * other number like '①⑩⑽㈩' 
	 */
	int T_OTHER_NUMBER = 7;
	
	/**
	 * pinyin 
	 */
	int T_CJK_PINYIN = 8;
	
	/**
	 * Chinese numeric */
	int T_CN_NUMERIC = 9;
	
	int T_PUNCTUATION = 10;
	
	/**
	 * useless chars like the CJK punctuation
	 */
	int T_UNRECOGNIZE_WORD = 11;
	
	
	/**
	 * return the value of the word
	 * 
	 * @return String
	 */
	String getValue();
	
	/**
	 * return the length of the word
	 * 
	 * @return int
	 */
	int getLength();
	
	/**
	 * return the frequency of the word,
	 * 	use only when the word's length is one.
	 * 
	 * @return int
	 */
	int getFrequency();
	
	/**
	 * return the type of the word
	 * 
	 * @return int
	 */
	int getType();
	
	/**
	 * set the position of the word
	 * 
	 * @param pos
	 */
	void setPosition(int pos);
	
	/**
	 * return the start position of the word.
	 * 
	 * @return int
	 */
	int getPosition();
	
	/**
	 * return the pinying of the word 
	 */
	String getPinyin();
	
	/**
	 * return the syn words of the word.
	 * 
	 * @return String[]
	 */
	String[] getSyn();
	
	void setSyn(String[] syn);
	
	/**
	 * return the part of speech of the word.
	 * 
	 * @return String[]
	 */
	String[] getPartSpeech();
	
	void setPartSpeech(String[] ps);
	
	/**
	 * set the pinying of the word
	 * 
	 * @param py
	 */
	void setPinyin(String py);
	
	/**
	 * add a new part to speech to the word.
	 * 
	 * @param ps
	 */
	void addPartSpeech(String ps);
	
	/**
	 * add a new syn word to the word.
	 * 
	 * @param s
	 */
	void addSyn(String s);
}
