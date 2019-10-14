package com.talkweb.common.tools.sort;

import org.apache.commons.lang.StringUtils;

/**
 * @ClassName NumberSort.java
 * @author liboqi
 * @version 1.0
 * @Description 数字排序
 * @date 2015年12月18日 下午5:47:21
 */
public class NumberSort {
	
	public static void halfSortByJx(String[] array) {
        int low, high, mid;
        int tmp, j;
        for (int i = 1; i < array.length; i++) {
        	String strZe = "";
        	boolean startsWith = array[i].startsWith("0");
        	if(startsWith){
        		String[] split = array[i].split("");
        		for (int k = 0; k < split.length; k++) {
					String string = split[k];
					if(StringUtils.isNotBlank(string)){
						if(Integer.valueOf(string) == 0){
							strZe = strZe + string;
						}else{
							break;
						}
					}
				}
        	}
            tmp = Integer.valueOf(array[i]);
            low = 0;
            high = i - 1;
            while (low <= high) {
                mid = (low + high) >>> 1;
                if (Integer.valueOf(array[mid]) < tmp)
                    high = mid - 1;
                else
                    low = mid + 1;
            }
            for (j = i - 1; j > high; j--) {
                array[j + 1] = array[j];
            }
            if(startsWith){
            	strZe = strZe + String.valueOf(tmp);
            	array[high + 1] = String.valueOf(strZe);
            }else{
            	array[high + 1] = String.valueOf(tmp);
            }
        }
    }
	
	public static void halfSortBySx(String[] array) {
        int low, high, mid;
        long tmp;
        int j;
        for (int i = 1; i < array.length; i++) {
        	String strZe = "";
        	boolean startsWith = array[i].startsWith("0");
        	if(startsWith){
        		String[] split = array[i].split("");
        		for (int k = 0; k < split.length; k++) {
					String string = split[k];
					if(StringUtils.isNotBlank(string)){
						if(Integer.valueOf(string) == 0){
							strZe = strZe + string;
						}else{
							break;
						}
					}
				}
        	}
            tmp = Long.valueOf(array[i]);
            low = 0;
            high = i - 1;
            while (low <= high) {
                mid = (low + high) >>> 1;
                if (Long.valueOf(array[mid]) > tmp)
                    high = mid - 1;
                else
                    low = mid + 1;
            }
            for (j = i - 1; j > high; j--) {
                array[j + 1] = array[j];
            }
            if(startsWith){
            	strZe = strZe + String.valueOf(tmp);
            	array[high + 1] = String.valueOf(strZe);
            }else{
            	array[high + 1] = String.valueOf(tmp);
            }
        }
    }
	public static boolean isNumeric(String str){
		  for (int i = 0; i < str.length(); i++){
			   if (!Character.isDigit(str.charAt(i))){
				   return false;
			   }
		  }
		  return true;
	 }

	
}
