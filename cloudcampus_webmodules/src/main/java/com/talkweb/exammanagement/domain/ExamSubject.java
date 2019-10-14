package com.talkweb.exammanagement.domain;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;

public class ExamSubject {
	private String examSubjectId;
	private Long schoolId;
	private String examManagementId;
	private String examPlanId;
	private Long subjectId;
	private Integer subjectLevel;
	private	String examSubjName;
	private String examSubjSimpleName;
	private Date startTime;
	private Date endTime;
	private String termInfo;
	private Integer scene;

	public String getExamSubjectId() {
		return examSubjectId;
	}

	public void setExamSubjectId(String examSubjectId) {
		this.examSubjectId = examSubjectId;
	}

	public Long getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(Long schoolId) {
		this.schoolId = schoolId;
	}

	public String getExamManagementId() {
		return examManagementId;
	}

	public void setExamManagementId(String examManagementId) {
		this.examManagementId = examManagementId;
	}

	public String getExamPlanId() {
		return examPlanId;
	}

	public void setExamPlanId(String examPlanId) {
		this.examPlanId = examPlanId;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	public Integer getSubjectLevel() {
		return subjectLevel;
	}

	public void setSubjectLevel(Integer subjectLevel) {
		this.subjectLevel = subjectLevel;
	}

	public String getExamSubjName() {
		return examSubjName;
	}

	public void setExamSubjName(String examSubjName) {
		this.examSubjName = examSubjName;
	}

	public String getExamSubjSimpleName() {
		return examSubjSimpleName;
	}

	public void setExamSubjSimpleName(String examSubjSimpleName) {
		this.examSubjSimpleName = examSubjSimpleName;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}

	public Integer getScene() {
		return scene;
	}

	public void setScene(Integer scene) {
		this.scene = scene;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
