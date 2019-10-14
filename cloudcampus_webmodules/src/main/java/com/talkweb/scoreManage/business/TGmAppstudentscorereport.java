package com.talkweb.scoreManage.business;

/**********
 * 
 * table T_GM_AppStudentScoreReport ->TGmAppstudentscorereport
 * 
 * @author administator
 *
 */
public class TGmAppstudentscorereport {

	public String getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}

	public String getExamId() {
		return examId;
	}

	public void setExamId(String examId) {
		this.examId = examId;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public String getReportData() {
		return reportData;
	}

	public void setReportData(String reportData) {
		this.reportData = reportData;
	}

	private String schoolId;
	private String examId;
	private String studentId;
	private String reportData;

}
