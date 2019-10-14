package com.talkweb.exammanagement.domain;

import com.alibaba.fastjson.JSONObject;

public class ArrangeExamResult implements Cloneable {
	private String examManagementId;
	private String termInfo;
	private Long schoolId;
	
	private String examSubjectGroupId;
	private String examPlaceId;
	private int seatNumber;
	private String examPlanId;
	private String tClassId;
	private Long accountId;
	private String testNumber;
	
	private Long tClassNo;	// 教学班级班号，辅助字段
	private String tClassName; // 教学班级名称，辅助字段
	private double score = 0;

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

	public int getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(int seatNumber) {
		this.seatNumber = seatNumber;
	}

	public String getExamPlanId() {
		return examPlanId;
	}

	public void setExamPlanId(String examPlanId) {
		this.examPlanId = examPlanId;
	}

	public String gettClassId() {
		return tClassId;
	}

	public void settClassId(String tClassId) {
		this.tClassId = tClassId;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public String getTestNumber() {
		return testNumber;
	}

	public void setTestNumber(String testNumber) {
		this.testNumber = testNumber;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}

	public String gettClassName() {
		return tClassName;
	}

	public void settClassName(String tClassName) {
		this.tClassName = tClassName;
	}

	public Long gettClassNo() {
		return tClassNo;
	}

	public void settClassNo(Long tClassNo) {
		this.tClassNo = tClassNo;
	}
	
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return (ArrangeExamResult) super.clone();
	}
}
