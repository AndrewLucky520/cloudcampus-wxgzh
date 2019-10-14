package com.talkweb.common.tools.sort;
/**
 * @ClassName SortEnum.java
 * @author liboqi
 * @version 1.0
 * @Description 排序类型枚举
 * @date 2015年12月29日 上午10:18:23
 */
public enum SortEnum {
	descEndingOrder("false"),ascEnding0rder("true"),
	numberOrder("3"),romeOrder("4"),metalOrder("5"),
	letterOrder("6"),chineseOrder("7");
	private String value;
	
	private SortEnum(String value){
		this.value = value;
	}
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String toString(){
		return this.value;
	}
}
