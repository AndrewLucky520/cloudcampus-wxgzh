package com.talkweb.timetable.dynamicProgram.rule;

import java.io.Serializable;

public class SchRuleResearchMeeting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4677473990714331173L;
	
	private String teacherId;
	
	private String teacherName;
	
	private int day;
	
	private int lesson;

	public String getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(String teacherId) {
		this.teacherId = teacherId;
	}

	public String getTeacherName() {
		return teacherName;
	}

	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
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
