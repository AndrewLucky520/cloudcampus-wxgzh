package com.talkweb.exammanagement.domain;

public class StudNotTakingExam {
	private Long schoolId;
	private String examManagementId;
	private String examPlanId;
	private String tClassId;
	private Long accountId;
	private String examSubjectId;
	private String termInfo;

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

	public String getExamSubjectId() {
		return examSubjectId;
	}

	public void setExamSubjectId(String examSubjectId) {
		this.examSubjectId = examSubjectId;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}
}
