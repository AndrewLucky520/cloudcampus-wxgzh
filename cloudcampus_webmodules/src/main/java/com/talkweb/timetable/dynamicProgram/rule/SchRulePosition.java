package com.talkweb.timetable.dynamicProgram.rule;

import java.io.Serializable;

public class SchRulePosition implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3344975977511404197L;

	//0：不排位置，1：必排位置
	private int ruleType;
	private int day;
	private int lesson;
	
	public int getRuleType() {
		return ruleType;
	}
	public void setRuleType(int ruleType) {
		this.ruleType = ruleType;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public int getLesson() {
		return lesson;
	}
	public void setLesson(int lesson) {
		this.lesson = lesson;
	}
	

}
