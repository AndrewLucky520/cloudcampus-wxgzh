package com.talkweb.placementtask.domain;

/**
 * 中走班的基本设置表
 * 对应t_pl_medium_settings表实体类
 */
public class TPlMediumSettings {
	private String ruleId;
	private String placementId;
	private String schoolId;
	private String termInfo;
	private Integer ruleCode;
	private String examId;
	private String examTermInfo;
	private Integer maxClassNum;
	private Integer gradeSumLesson;
	private Integer fixedSumLesson;
	public String getRuleId() {
		return ruleId;
	}
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}
	public String getPlacementId() {
		return placementId;
	}
	public void setPlacementId(String placementId) {
		this.placementId = placementId;
	}
	public String getSchoolId() {
		return schoolId;
	}
	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}
	public String getTermInfo() {
		return termInfo;
	}
	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}
	public Integer getRuleCode() {
		return ruleCode;
	}
	public void setRuleCode(Integer ruleCode) {
		this.ruleCode = ruleCode;
	}
	public String getExamId() {
		return examId;
	}
	public void setExamId(String examId) {
		this.examId = examId;
	}
	public String getExamTermInfo() {
		return examTermInfo;
	}
	public void setExamTermInfo(String examTermInfo) {
		this.examTermInfo = examTermInfo;
	}
	public Integer getMaxClassNum() {
		return maxClassNum;
	}
	public void setMaxClassNum(Integer maxClassNum) {
		this.maxClassNum = maxClassNum;
	}
	public Integer getGradeSumLesson() {
		return gradeSumLesson;
	}
	public void setGradeSumLesson(Integer gradeSumLesson) {
		this.gradeSumLesson = gradeSumLesson;
	}
	public Integer getFixedSumLesson() {
		return fixedSumLesson;
	}
	public void setFixedSumLesson(Integer fixedSumLesson) {
		this.fixedSumLesson = fixedSumLesson;
	}
}
