package com.talkweb.weekWork.pojo;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.fastjson.JSONObject;

public class WeeklyRecord implements Serializable {
	private static final long serialVersionUID = 3156124867180136672L;
	private Long schoolId;
	private String schoolYear;
	private String term;
	private Integer week;
	private String departmentId;
	private String departmentName;
	private String teacherId;
	private Date submitTime;
	private String content;
	private Integer version;
	private String teacherIdFill;

	public Long getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(Long schoolId) {
		this.schoolId = schoolId;
	}

	public String getSchoolYear() {
		return schoolYear;
	}

	public void setSchoolYear(String schoolYear) {
		this.schoolYear = schoolYear;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public Integer getWeek() {
		return week;
	}

	public void setWeek(Integer week) {
		this.week = week;
	}

	public String getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(String departmentId) {
		this.departmentId = departmentId;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(String teacherId) {
		this.teacherId = teacherId;
	}

	public Date getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(Date submitTime) {
		this.submitTime = submitTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getTeacherIdFill() {
		return teacherIdFill;
	}

	public void setTeacherIdFill(String teacherIdFill) {
		this.teacherIdFill = teacherIdFill;
	}
	
	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
