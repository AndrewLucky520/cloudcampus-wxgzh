package com.talkweb.scoreManage.po.ce;

import com.alibaba.fastjson.JSONObject;

public class CustomScore {
	private String ExamId; // 考试轮次代码
	private String SchoolId; // 学校代码
	private String ProjectId; // 导入项目序号
	private String ProjectName; // 导入项目名称
	private String ParentProjectId; // 父项目序号
	private Integer RowSpan; // 行跨度
	private Integer ColSpan; // 列跨度
	private Integer Row; // 所在行
	private Integer Col; // 所在列
	private Integer TitleCount; // 表头行
	private String InputTime; // 输入时间
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

	public String getProjectName() {
		return ProjectName;
	}

	public void setProjectName(String projectName) {
		ProjectName = projectName;
	}

	public String getParentProjectId() {
		return ParentProjectId;
	}

	public void setParentProjectId(String parentProjectId) {
		ParentProjectId = parentProjectId;
	}

	public Integer getRowSpan() {
		return RowSpan;
	}

	public void setRowSpan(Integer rowSpan) {
		RowSpan = rowSpan;
	}

	public Integer getColSpan() {
		return ColSpan;
	}

	public void setColSpan(Integer colSpan) {
		ColSpan = colSpan;
	}

	public Integer getRow() {
		return Row;
	}

	public void setRow(Integer row) {
		Row = row;
	}

	public Integer getCol() {
		return Col;
	}

	public void setCol(Integer col) {
		Col = col;
	}

	public Integer getTitleCount() {
		return TitleCount;
	}

	public void setTitleCount(Integer titleCount) {
		TitleCount = titleCount;
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
