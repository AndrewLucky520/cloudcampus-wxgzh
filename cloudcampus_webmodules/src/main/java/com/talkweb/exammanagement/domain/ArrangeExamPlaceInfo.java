package com.talkweb.exammanagement.domain;

import com.alibaba.fastjson.JSONObject;

public class ArrangeExamPlaceInfo {
	private String examManagementId;
	private Long schoolId;
	private String termInfo;
	
	private String examSubjectGroupId;
	private String examPlaceId;
	private Integer remainNumOfStuds;
	private Integer numOfStuds;
	
	private String examPlaceCode;

	public String getExamSubjectGroupId() {
		return examSubjectGroupId;
	}

	public void setExamSubjectGroupId(String examSubjectGroupId) {
		this.examSubjectGroupId = examSubjectGroupId;
	}

	public String getExamPlaceId() {
		return examPlaceId;
	}

	public void setExamPlaceId(String examPlaceId) {
		this.examPlaceId = examPlaceId;
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

	public Integer getRemainNumOfStuds() {
		return remainNumOfStuds;
	}

	public void setRemainNumOfStuds(Integer remainNumOfStuds) {
		this.remainNumOfStuds = remainNumOfStuds;
	}

	public Integer getNumOfStuds() {
		return numOfStuds;
	}

	public void setNumOfStuds(Integer numOfStuds) {
		this.numOfStuds = numOfStuds;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}
	
	public String getExamPlaceCode() {
		return examPlaceCode;
	}

	public void setExamPlaceCode(String examPlaceCode) {
		this.examPlaceCode = examPlaceCode;
	}

	public String toString(){
		return JSONObject.toJSONString(this);
	}
}
