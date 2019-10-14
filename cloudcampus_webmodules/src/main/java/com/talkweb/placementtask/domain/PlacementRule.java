package com.talkweb.placementtask.domain;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

public class PlacementRule implements Serializable {
	private static final long serialVersionUID = -6845880419042688954L;
	
	private String ruleId;
	private Long schoolId;
	private String placementId;
	
	private String examId;
	private String examTermInfo;
	private Integer ruleCode;
	
	private String termInfo;

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public Long getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(Long schoolId) {
		this.schoolId = schoolId;
	}

	public String getPlacementId() {
		return placementId;
	}

	public void setPlacementId(String placementId) {
		this.placementId = placementId;
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

	public Integer getRuleCode() {
		return ruleCode;
	}

	public void setRuleCode(Integer ruleCode) {
		this.ruleCode = ruleCode;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}
	
	@Override
	public String toString(){
		return JSON.toJSONString(this);
	}
}
