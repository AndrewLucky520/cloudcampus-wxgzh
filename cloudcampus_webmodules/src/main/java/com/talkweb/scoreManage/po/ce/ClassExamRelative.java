package com.talkweb.scoreManage.po.ce;

import com.alibaba.fastjson.JSONObject;

public class ClassExamRelative {
	private String ExamId; // 考试轮次代码
	private String ClassId; // 班级代码
	private String SchoolId; // 学校代码
	private String InputTime; // 输入时间
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
