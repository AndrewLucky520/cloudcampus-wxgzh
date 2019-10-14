package com.talkweb.exammanagement.domain;

import com.alibaba.fastjson.JSONObject;

public class ArrangeExamSubjectInfo {
	private String examManagementId;
	private Long schoolId;
	private String termInfo;

	private String examSubjectGroupId;
	private String examSubjectId;

	private Integer sort;

	public String getExamManagementId() {
		return examManagementId;
	}

	public void setExamManagementId(String examManagementId) {
		this.examManagementId = examManagementId;
	}

	public Long getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(Long schoolId) {
		this.schoolId = schoolId;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}

	public String getExamSubjectGroupId() {
		return examSubjectGroupId;
	}

	public void setExamSubjectGroupId(String examSubjectGroupId) {
		this.examSubjectGroupId = examSubjectGroupId;
	}

	public String getExamSubjectId() {
		return examSubjectId;
	}

	public void setExamSubjectId(String examSubjectId) {
		this.examSubjectId = examSubjectId;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}
	
	public String toString(){
		return JSONObject.toJSONString(this);
	}
}
