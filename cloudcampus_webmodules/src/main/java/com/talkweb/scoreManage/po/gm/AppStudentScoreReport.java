package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// App学生成绩报告
public class AppStudentScoreReport {
	private String SchoolId; // 学校代码
	private String ExamId; // 考试轮次
	private String StudentId; // 学生代码
	private String ReportData; // 报告数据
	private String termInfoId; // 学年学期

	public String getSchoolId() {
		return SchoolId;
	}

	public void setSchoolId(String schoolId) {
		SchoolId = schoolId;
	}

	public String getExamId() {
		return ExamId;
	}

	public void setExamId(String examId) {
		ExamId = examId;
	}

	public String getStudentId() {
		return StudentId;
	}

	public void setStudentId(String studentId) {
		StudentId = studentId;
	}

	public String getReportData() {
		return ReportData;
	}

	public void setReportData(String reportData) {
		ReportData = reportData;
	}

	public String getTermInfoId() {
		return termInfoId;
	}

	public void setTermInfoId(String termInfoId) {
		this.termInfoId = termInfoId;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
