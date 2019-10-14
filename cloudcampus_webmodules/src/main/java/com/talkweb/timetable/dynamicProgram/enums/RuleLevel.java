package com.talkweb.timetable.dynamicProgram.enums;

/**
 * 规则等级
 * @author talkweb
 *
 */
public enum RuleLevel {
	// 非常重要--做控制,不满足不排课
	Important (1),
	// 尽量满足--不做控制,仅做优化条件
	LessImportant(2),
	// 不重要--不做控制,仅做次要优化条件
	UnImportant(3) ,
	// 完全不需要--目前不处理
	NoNeed(4),
	// 与规则相反处理--目前不处理
	Opposite  (-1);

	private int level;
	
	private RuleLevel(int level){
		this.level = level;
	}
	
	public int getValue( ){
		return this.level;
	}
	
	public static void main(String[] args) {
		System.out.println(RuleLevel.Important.getValue( ));
	}
}
