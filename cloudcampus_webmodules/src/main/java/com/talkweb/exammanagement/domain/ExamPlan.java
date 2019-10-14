package com.talkweb.exammanagement.domain;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class ExamPlan {
	private String examPlanId;
	private String examManagementId;
	private Long schoolId;
	private String usedGrade;
	private String scheduleId;
	private String termInfo;
	private String testNumPrefix;
	private Integer gradeDigit = 2;
	private Integer serialNumDigit = 4;
	private Integer status;

	private List<ExamSubject> examSubjectList = new ArrayList<ExamSubject>();

	public String getExamPlanId() {
		return examPlanId;
	}

	public void setExamPlanId(String examPlanId) {
		this.examPlanId = examPlanId;
	}

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

	public String getUsedGrade() {
		return usedGrade;
	}

	public void setUsedGrade(String usedGrade) {
		this.usedGrade = usedGrade;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}

	public String getTestNumPrefix() {
		return testNumPrefix;
	}

	public void setTestNumPrefix(String testNumPrefix) {
		this.testNumPrefix = testNumPrefix;
	}

	public Integer getGradeDigit() {
		return gradeDigit;
	}

	public void setGradeDigit(Integer gradeDigit) {
		this.gradeDigit = gradeDigit;
	}

	public Integer getSerialNumDigit() {
		return serialNumDigit;
	}

	public void setSerialNumDigit(Integer serialNumDigit) {
		this.serialNumDigit = serialNumDigit;
	}

	public List<ExamSubject> getExamSubjectList() {
		return examSubjectList;
	}

	public void setExamSubjectList(List<ExamSubject> examSubjectList) {
		this.examSubjectList = examSubjectList;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
