package com.talkweb.exammanagement.domain;

public class TestNumberInfo {
	private String examManagementId;
	private Long schoolId;
	private String testNumber;
	private String usedGrade;
	private Long accountId;
	private String termInfo;

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

	public String getTestNumber() {
		return testNumber;
	}

	public void setTestNumber(String testNumber) {
		this.testNumber = testNumber;
	}

	public String getUsedGrade() {
		return usedGrade;
	}

	public void setUsedGrade(String usedGrade) {
		this.usedGrade = usedGrade;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}
}
