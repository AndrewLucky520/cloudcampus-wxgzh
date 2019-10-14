package com.talkweb.timetable.dynamicProgram.entity;

import java.io.Serializable;
import java.util.List;

public class GridPoint implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9176253027487891395L;

	private String taskGroupId;
	private String gradeId;
	private String gradeLevel;
	private String classId;
	private String courseId;
	private List<String> teacherIds;
	private double arrangedNum;

	private int day;
	private int lesson;
	private int courseType;
	
	private boolean isAdvance = false;
	
	private boolean isNeedCourseNumControl ;
	/**
	 * 是否连排课
	 */
	private boolean isSpPoint = false;

	private String mcGroupId ;
	public String getTaskGroupId() {
		return taskGroupId;
	}

	public void setTaskGroupId(String taskGroupId) {
		this.taskGroupId = taskGroupId;
	}

	public String getGradeId() {
		return gradeId;
	}

	public void setGradeId(String gradeId) {
		this.gradeId = gradeId;
	}

	public String getGradeLevel() {
		return gradeLevel;
	}

	public void setGradeLevel(String gradeLevel) {
		this.gradeLevel = gradeLevel;
	}

	public String getClassId() {
		return classId;
	}

	public void setClassId(String classId) {
		this.classId = classId;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public List<String> getTeacherIds() {
		return teacherIds;
	}

	public void setTeacherIds(List<String> teacherIds) {
		this.teacherIds = teacherIds;
	}

	public double getArrangedNum() {
		return arrangedNum;
	}

	public void setArrangedNum(double arrangedNum) {
		this.arrangedNum = arrangedNum;
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

	public int getCourseType() {
		return courseType;
	}

	public void setCourseType(int courseType) {
		this.courseType = courseType;
	}

	public boolean isAdvance() {
		return isAdvance;
	}

	public void setAdvance(boolean isAdvance) {
		this.isAdvance = isAdvance;
	}

	public boolean isNeedCourseNumControl() {
		return isNeedCourseNumControl;
	}

	public void setNeedCourseNumControl(boolean isNeedCourseNumControl) {
		this.isNeedCourseNumControl = isNeedCourseNumControl;
	}

	public boolean isSpPoint() {
		return isSpPoint;
	}

	public void setSpPoint(boolean isSpPoint) {
		this.isSpPoint = isSpPoint;
	}

	public String getMcGroupId() {
		// TODO Auto-generated method stub
		return mcGroupId;
	}

	public void setMcGroupId(String mcGroupId) {
		this.mcGroupId = mcGroupId;
	}
	
	
}
