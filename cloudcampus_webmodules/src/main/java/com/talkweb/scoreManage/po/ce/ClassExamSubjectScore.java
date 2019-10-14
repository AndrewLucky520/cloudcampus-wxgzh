package com.talkweb.scoreManage.po.ce;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

public class ClassExamSubjectScore implements Serializable {
	private static final long serialVersionUID = -4831285069076999104L;
	private String ExamId; // 考试轮次
	private String StudentId; // 学生代码
	private String SubjectId; // 科目代码
	private String ClassId; // 班级代码
	private String SchoolId; // 学校代码
	private Float Score; // 分数
	private String inputTime; // 输入日期
	private String score2; // 分数
	private String termInfoId; // 学年学期

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

	public String getSubjectId() {
		return SubjectId;
	}

	public void setSubjectId(String subjectId) {
		SubjectId = subjectId;
	}

	public String getClassId() {
		return ClassId;
	}

	public void setClassId(String classId) {
		ClassId = classId;
	}

	public String getSchoolId() {
		return SchoolId;
	}

	public void setSchoolId(String schoolId) {
		SchoolId = schoolId;
	}

	public Float getScore() {
		return Score;
	}

	public void setScore(Float score) {
		Score = score;
	}

	public String getInputTime() {
		return inputTime;
	}

	public void setInputTime(String inputTime) {
		this.inputTime = inputTime;
	}

	public String getScore2() {
		return score2;
	}

	public void setScore2(String score2) {
		this.score2 = score2;
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
