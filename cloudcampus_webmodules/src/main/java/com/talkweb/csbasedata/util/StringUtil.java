package com.talkweb.csbasedata.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StringUtil {
	
	/***
	 * 判断空字符窜
	 * @param source
	 * @return
	 */
	public static boolean isEmpty(String source)
	{
		if(source==null||"".equals(source)){
			return true;
		}
		
		return false;
	}
	public static String createRandom(int length){
		String s= (Math.random() + "").replace(".", "");
		return s.substring(0,length);
	}
	/** 过滤中文/半角|全角/特殊字符 */
	public static String replaceChar(String objective){
		objective = objective.replace("\u0020", "");
		objective = objective.replace("\u3000", "");
		objective = objective.replace("\u00A0", "");
		objective = objective.replace("，", ",");
		objective = objective.replace("\\u000A", "");
		objective = objective.replace("\\u000D", "");
		objective = objective.replace("\u0008", "");
//		objective = objective.replace("（", "(");
//		objective = objective.replace("）", ")");
//		objective = objective.replace("【", "[");
//		objective = objective.replace("】", "]");
//		objective = objective.replace("｛", "{");
//		objective = objective.replace("｝", "}");
//		objective = objective.replace("《", "<");
//		objective = objective.replace("》", ">");
		return objective;
	}
	  /** 
     * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数 
     * 此方法中前三位格式有： 
     * 13+任意数 
     * 15+除4的任意数 
     * 18+除1和4的任意数 
     * 17+除9的任意数 
     * 147 
     */  
    public static boolean isChinaPhoneLegal(String str) throws PatternSyntaxException {  
        String regExp = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";  
        Pattern p = Pattern.compile(regExp);  
        Matcher m = p.matcher(str);  
        return m.matches();  
    }  
	public static void main(String[] args) {
		Boolean s = StringUtil.isChinaPhoneLegal("13907334949");
		System.out.println(s);
	}
}