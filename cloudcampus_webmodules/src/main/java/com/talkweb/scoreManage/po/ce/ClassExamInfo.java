package com.talkweb.scoreManage.po.ce;

import com.alibaba.fastjson.JSONObject;

public class ClassExamInfo {
	private String ExamId; // 考试id
	private String ExamName; // 考试名称
	private Integer autoIncr; // 自增长变量
	private String SchoolId; // 学校代码
	private Integer IsImport; // 是否导入数据
	private String CreateUserId; // 创建者代码
	private String CreateTime; // 创建时间
	private String UpdaeTime; // 修改时间
	private Integer isPublic; // 是否已发布
	private Integer scoreDataType; // 数据类型，当examType=1是，0为非数值类型，1为数值类型
	private Integer examType; // 考试类型，1：系统格式，2：自定义考试
	private String PublicTime; // 发布时间
	private String termInfoId; // 学年学期
	private Integer counter;

	public String getExamId() {
		return ExamId;
	}

	public void setExamId(String examId) {
		ExamId = examId;
	}

	public String getExamName() {
		return ExamName;
	}

	public void setExamName(String examName) {
		ExamName = examName;
	}

	public Integer getAutoIncr() {
		return autoIncr;
	}

	public void setAutoIncr(Integer autoIncr) {
		this.autoIncr = autoIncr;
	}

	public String getSchoolId() {
		return SchoolId;
	}

	public void setSchoolId(String schoolId) {
		SchoolId = schoolId;
	}

	public Integer getIsImport() {
		return IsImport;
	}

	public void setIsImport(Integer isImport) {
		IsImport = isImport;
	}

	public String getCreateUserId() {
		return CreateUserId;
	}

	public void setCreateUserId(String createUserId) {
		CreateUserId = createUserId;
	}

	public String getCreateTime() {
		return CreateTime;
	}

	public void setCreateTime(String createTime) {
		CreateTime = createTime;
	}

	public String getUpdaeTime() {
		return UpdaeTime;
	}

	public void setUpdaeTime(String updaeTime) {
		UpdaeTime = updaeTime;
	}

	public Integer getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Integer isPublic) {
		this.isPublic = isPublic;
	}

	public Integer getScoreDataType() {
		return scoreDataType;
	}

	public void setScoreDataType(Integer scoreDataType) {
		this.scoreDataType = scoreDataType;
	}

	public Integer getExamType() {
		return examType;
	}

	public void setExamType(Integer examType) {
		this.examType = examType;
	}

	public String getPublicTime() {
		return PublicTime;
	}

	public void setPublicTime(String publicTime) {
		PublicTime = publicTime;
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

	public Integer getCounter() {
		return counter;
	}

	public void setCounter(Integer counter) {
		this.counter = counter;
	}
}
