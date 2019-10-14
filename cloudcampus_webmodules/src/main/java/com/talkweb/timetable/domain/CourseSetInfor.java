package com.talkweb.timetable.domain;

import java.io.Serializable;

/** 
* @author  Administrator
* @version 创建时间：2017年1月13日 上午10:44:45 
* 程序的简单说明
*/
public class CourseSetInfor  implements Serializable{
 
	private static final long serialVersionUID = 1L;
	
	private String courseId;
	private String courseName;
	private double sysSet; 
	private double userSet;
	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	public double getSysSet() {
		return sysSet;
	}
	public void setSysSet(double sysSet) {
		this.sysSet = sysSet;
	}
	public double getUserSet() {
		return userSet;
	}
	public void setUserSet(double userSet) {
		this.userSet = userSet;
	}
 
	
	
 
}
