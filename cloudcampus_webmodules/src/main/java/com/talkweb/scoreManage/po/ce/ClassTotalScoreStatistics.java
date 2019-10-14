package com.talkweb.scoreManage.po.ce;

import com.alibaba.fastjson.JSONObject;

public class ClassTotalScoreStatistics {
	private String ExamId; // 考试代码
	private String ClassId; // 班级代码
	private String SchoolId; // 学校代码
	private Integer ClassStudentNum; // 班级学生人数
	private Float ClassTotalScoreAverScore; // 班级平均总分
	private Float ClassTopTotalScore; // 班级最高总分
	private Float TopOneScore; // 班级总分第一名分数
	private Float TopTwoScore; // 班级总分第二名分数
	private Float TopThreeScore; // 班级总分第三名分数
	private String inputTime; // 输入时间
	private String termInfoId; // 学年学期

	public String getExamId() {
		return ExamId;
	}

	public void setExamId(String examId) {
		ExamId = examId;
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

	public Integer getClassStudentNum() {
		return ClassStudentNum;
	}

	public void setClassStudentNum(Integer classStudentNum) {
		ClassStudentNum = classStudentNum;
	}

	public Float getClassTotalScoreAverScore() {
		return ClassTotalScoreAverScore;
	}

	public void setClassTotalScoreAverScore(Float classTotalScoreAverScore) {
		ClassTotalScoreAverScore = classTotalScoreAverScore;
	}

	public Float getClassTopTotalScore() {
		return ClassTopTotalScore;
	}

	public void setClassTopTotalScore(Float classTopTotalScore) {
		ClassTopTotalScore = classTopTotalScore;
	}

	public Float getTopOneScore() {
		return TopOneScore;
	}

	public void setTopOneScore(Float topOneScore) {
		TopOneScore = topOneScore;
	}

	public Float getTopTwoScore() {
		return TopTwoScore;
	}

	public void setTopTwoScore(Float topTwoScore) {
		TopTwoScore = topTwoScore;
	}

	public Float getTopThreeScore() {
		return TopThreeScore;
	}

	public void setTopThreeScore(Float topThreeScore) {
		TopThreeScore = topThreeScore;
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
