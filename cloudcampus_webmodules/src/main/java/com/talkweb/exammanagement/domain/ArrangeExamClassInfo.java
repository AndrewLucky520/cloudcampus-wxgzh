package com.talkweb.exammanagement.domain;

import com.alibaba.fastjson.JSONObject;

public class ArrangeExamClassInfo {
	private String examManagementId;
	private Long schoolId;
	private String termInfo;
	
	private String examSubjectGroupId;
	private String usedGrade;
	private String tClassId;

	public String getExamSubjectGroupId() {
		return examSubjectGroupId;
	}

	public void setExamSubjectGroupId(String examSubjectGroupId) {
		this.examSubjectGroupId = examSubjectGroupId;
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

	public String getUsedGrade() {
		return usedGrade;
	}

	public void setUsedGrade(String usedGrade) {
		this.usedGrade = usedGrade;
	}

	public String gettClassId() {
		return tClassId;
	}

	public void settClassId(String tClassId) {
		this.tClassId = tClassId;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}
	
	public String toString(){
		return JSONObject.toJSONString(this);
	}
}
