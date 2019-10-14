package com.talkweb.scoreManage.po.ce;

import com.alibaba.fastjson.JSONObject;

public class StudentTotalScoreStatistics {
	private String ExamId; // 考试轮次
	private String StudentId; // 学生代码
	private String ClassId; // 班级代码
	private String SchoolId; // 学校代码
	private Float TotalScore; // 总分数
	private Integer TotalScoreRank; // 总分排名
	private Integer surpassRatio; // 击败人数比例
	private Integer ClassStudentNum; // 班级学生数
	private String inputTime; // 输入时间
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

	public Float getTotalScore() {
		return TotalScore;
	}

	public void setTotalScore(Float totalScore) {
		TotalScore = totalScore;
	}

	public Integer getTotalScoreRank() {
		return TotalScoreRank;
	}

	public void setTotalScoreRank(Integer totalScoreRank) {
		TotalScoreRank = totalScoreRank;
	}

	public Integer getSurpassRatio() {
		return surpassRatio;
	}

	public void setSurpassRatio(Integer surpassRatio) {
		this.surpassRatio = surpassRatio;
	}

	public Integer getClassStudentNum() {
		return ClassStudentNum;
	}

	public void setClassStudentNum(Integer classStudentNum) {
		ClassStudentNum = classStudentNum;
	}

	public String getInputTime() {
		return inputTime;
	}

	public void setInputTime(String inputTime) {
		this.inputTime = inputTime;
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
