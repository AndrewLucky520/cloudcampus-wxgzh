package com.talkweb.placementtask.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;

public class OpenClassInfo implements Serializable{
	private static final long serialVersionUID = -5171130737460394956L;

	private String openClassInfoId;
	private Long schoolId;
	private String placementId;
	
	private Integer placementType;
	private Integer type;
	
	private String zhName;
	private String subjectIdsStr;
	private String classIdsStr;
	
	private Float scoreUpLimit;
	private Float scoreDownLimit;
	
	private String termInfo;
	
	private String ruleId;
	
	private List<OpenClassTask> openClassTasks = new ArrayList<OpenClassTask>();

	public String getOpenClassInfoId() {
		return openClassInfoId;
	}

	public void setOpenClassInfoId(String openClassInfoId) {
		this.openClassInfoId = openClassInfoId;
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

	public Integer getPlacementType() {
		return placementType;
	}

	public void setPlacementType(Integer placementType) {
		this.placementType = placementType;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getZhName() {
		return zhName;
	}

	public void setZhName(String zhName) {
		this.zhName = zhName;
	}

	public String getSubjectIdsStr() {
		return subjectIdsStr;
	}

	public void setSubjectIdsStr(String subjectIdsStr) {
		this.subjectIdsStr = subjectIdsStr;
	}

	public String getClassIdsStr() {
		return classIdsStr;
	}

	public void setClassIdsStr(String classIdsStr) {
		this.classIdsStr = classIdsStr;
	}

	public Float getScoreUpLimit() {
		return scoreUpLimit;
	}

	public void setScoreUpLimit(Float scoreUpLimit) {
		this.scoreUpLimit = scoreUpLimit;
	}

	public Float getScoreDownLimit() {
		return scoreDownLimit;
	}

	public void setScoreDownLimit(Float scoreDownLimit) {
		this.scoreDownLimit = scoreDownLimit;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public List<OpenClassTask> getOpenClassTasks() {
		return openClassTasks;
	}

	public void setOpenClassTasks(List<OpenClassTask> openClassTasks) {
		this.openClassTasks = openClassTasks;
	}
	
	@Override
	public String toString(){
		return JSON.toJSONString(this);
	}
}
