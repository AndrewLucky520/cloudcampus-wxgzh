/**
 * 
 */
package com.talkweb.common.tools;

/**
 * @ClassName: ValidType
 * @version:1.0
 * @Description: 类型判断
 * @author 廖刚 ---智慧校
 * @date 2015年11月20日
 */
public class ValidType {

	/**
	 * 
	 */
	public ValidType() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 判断是否为int型
	 * @param value
	 * @return
	 */
	public static boolean isValidInt(String value) {
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 判断是否为Boolean型
	 * @param value
	 * @return
	 */
	public static boolean isBoolean(String value) {
		return Boolean.parseBoolean("value");
	}
	
}
