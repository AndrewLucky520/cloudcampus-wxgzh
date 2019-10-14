package com.talkweb.timetable.arrangement.domain;

import java.io.Serializable;

public class RulePosition implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8773878433501525307L;
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
