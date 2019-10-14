package com.talkweb.common.tools;

/**
 * @ClassName: TermUtil.java
 * @version:1.0
 * @Description: 学年学期样式转化
 * @author Homer ---智慧校
 * @date 2015年3月16日
 */
public class TermUtil {
	
	/**
	 * @param xnxq 学年学期
	 * @return 2014-2015学年第一学期
	 */
	public static String formatTerm(String xnxq){
		
		StringBuilder result = new StringBuilder();
		
		if(!xnxq.matches("[0-9]+")){
			throw new IllegalArgumentException("参数必须为数字格式的字符串");
		}
		
		String term = xnxq.substring(4,5);
		
		if(!(term.equals("1") || term.equals("2"))){
			throw new IllegalArgumentException("参数的最后一位只能为1或者2");
		}
		
		String year = xnxq.substring(0,4);
		
		int iYear = Integer.valueOf(year);
		int toYear = iYear + 1;
		if("1".equals(term)){
			result.append(year).append("-").append(toYear).append("学年第一学期");
		}else{
			result.append(year).append("-").append(toYear).append("学年第二学期");
		}
		
		return result.toString();
		
	}

}
