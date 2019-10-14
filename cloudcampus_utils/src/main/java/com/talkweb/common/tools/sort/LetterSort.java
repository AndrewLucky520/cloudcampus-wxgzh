package com.talkweb.common.tools.sort;

import org.apache.commons.lang.StringUtils;

/**
 * @ClassName NumberSort.java
 * @author liboqi
 * @version 1.0
 * @Description 字母排序
 * @date 2015年12月18日 下午5:37:13
 */
public class LetterSort {
	private static int compare(String s1, String s2) {
	    char[] arr1 = s1.toCharArray(), arr2 = s2.toCharArray();
	    int index = 0, len1 = arr1.length, len2 = arr2.length;
	    int len = len1 < len2 ? len1 : len2;
	    while (index < len) {
	        char c1 = arr1[index], c2 = arr2[index];
	        char c1_ = (char) (c1 >= 'a' ? c1 - ('a' - 'A') : c1);
	        char c2_ = (char) (c2 >= 'a' ? c2 - ('a' - 'A') : c2);
	        if (c1_ == c2_) {
	            if (c1 != c2)
	                return c1 - c2;
	        } else
	            return c1_ - c2_;
	        index++;
	    }
	    if (len1 == len2)
	        return 0;
	    else if (len1 > len2)
	        return arr1[len];
	    else
	        return -arr2[len];
	}
	public static void sortBySx(String[] src) {
	    String temp;
	    for (int i = 0; i < src.length - 1; i++) {
	        for (int j = 0; j < src.length - i - 1; j++) {
	            if (compare(src[j], src[j + 1]) > 0) {
	                temp = src[j];
	                src[j] = src[j + 1];
	                src[j + 1] = temp;
	            }
	        }
	    }
	}
	
	public static void sortByJx(String[] src) {
	    String temp;
	    for (int i = 0; i < src.length - 1; i++) {
	        for (int j = 0; j < src.length - i - 1; j++) {
	            if (compare(src[j], src[j + 1]) < 0) {
	                temp = src[j];
	                src[j] = src[j + 1];
	                src[j + 1] = temp;
	            }
	        }
	    }
	}
	
	public static boolean isLetter(String str){
		String reg = "[a-zA-Z]";
		String[] split = str.split("");
		boolean matches = true;
		for (String string : split) {
			if(StringUtils.isNotBlank(string)){
				matches = string.matches(reg);
				if(!matches){
					break;
				}
			}
		}
		return matches;
	}
}
