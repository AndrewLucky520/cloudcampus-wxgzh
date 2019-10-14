package com.talkweb.exammanagement.domain;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;

public class ArrangeExamRule {
	private String examManagementId;
	private String termInfo;
	private Long schoolId;
	private String examSubjectGroupId;
	private String examPlanId;
	private Integer numOfSubject;
	private Date createDateTime;

	public String getExamManagementId() {
		return examManagementId;
	}

	public void setExamManagementId(String examManagementId) {
		this.examManagementId = examManagementId;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}

	public Long getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(Long schoolId) {
		this.schoolId = schoolId;
	}

	public String getExamSubjectGroupId() {
		return examSubjectGroupId;
	}

	public void setExamSubjectGroupId(String examSubjectGroupId) {
		this.examSubjectGroupId = examSubjectGroupId;
	}

	public String getExamPlanId() {
		return examPlanId;
	}

	public void setExamPlanId(String examPlanId) {
		this.examPlanId = examPlanId;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public Integer getNumOfSubject() {
		return numOfSubject;
	}

	public void setNumOfSubject(Integer numOfSubject) {
		this.numOfSubject = numOfSubject;
	}
	
	public String toString(){
		return JSONObject.toJSONString(this);
	}
}
