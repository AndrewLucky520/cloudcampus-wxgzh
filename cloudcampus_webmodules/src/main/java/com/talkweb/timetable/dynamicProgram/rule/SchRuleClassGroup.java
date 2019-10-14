package com.talkweb.timetable.dynamicProgram.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SchRuleClassGroup implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2772451378169270398L;

	/**
	 * 合班组id McGroupId
	 */
	private String classGroupId;
	private String classGroupName;
	private String courseId;
	
	private List<String> classIds = new ArrayList<String>();
	
	
	public void addClassGroup(String classId){
		this.classIds.add(classId);
	}

	public String getClassGroupId() {
		return classGroupId;
	}

	public void setClassGroupId(String classGroupId) {
		this.classGroupId = classGroupId;
	}

	public String getClassGroupName() {
		return classGroupName;
	}

	public void setClassGroupName(String classGroupName) {
		this.classGroupName = classGroupName;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public List<String> getClassIds() {
		return classIds;
	}

	public void setClassIds(List<String> classIds) {
		this.classIds = classIds;
	}
}
