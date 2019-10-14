package com.talkweb.timetable.arrangement.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 合班规则
 *
 */
public class RuleClassGroup implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -384347975707654811L;
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
