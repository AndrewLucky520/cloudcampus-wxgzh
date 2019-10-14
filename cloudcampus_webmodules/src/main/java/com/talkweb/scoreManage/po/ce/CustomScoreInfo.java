package com.talkweb.scoreManage.po.ce;

import com.alibaba.fastjson.JSONObject;

public class CustomScoreInfo {
	private String ExamId; // 考试代码
	private String SchoolId; // 学校代码
	private String ProjectId; // 科目代码
	private String StudentId; // 学生代码
	private String ClassId; // 班级代码
	private String Score; // 分数
	private String InputTime; // 导入时间
	private String termInfoId; // 学年学期

	public String getExamId() {
		return ExamId;
	}

	public void setExamId(String examId) {
		ExamId = examId;
	}

	public String getSchoolId() {
		return SchoolId;
	}

	public void setSchoolId(String schoolId) {
		SchoolId = schoolId;
	}

	public String getProjectId() {
		return ProjectId;
	}

	public void setProjectId(String projectId) {
		ProjectId = projectId;
	}

	public String getStudentId() {
		return StudentId;
	}

	public void setStudentId(String studentId) {
		StudentId = studentId;
	}

	public String getClassId() {
		return ClassId;
	}

	public void setClassId(String classId) {
		ClassId = classId;
	}

	public String getScore() {
		return Score;
	}

	public void setScore(String score) {
		Score = score;
	}

	public String getInputTime() {
		return InputTime;
	}

	public void setInputTime(String inputTime) {
		InputTime = inputTime;
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
