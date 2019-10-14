package com.talkweb.placementtask.domain;

/**
 * 中走班的固定课程信息，保存各组合的固定班级以及人数设置
 * 对应数据库的t_pl_medium_zh_set表实体类
 */
public class TPlMediumZhSet {
	private String placementId;
	private String schoolId;
	private String termInfo;
	//分班任务id，和t_pl_openclassinfo中保持一致
	private String openClassInfoId;
	//组合ids(用,分隔)
	private String compFrom;
	//组合名
	private String compName;
	//组合实际选择人数
	private Integer compNum;
	//组合固定开班数
	private Integer numOfOpenClasses;
	//组合固定人数
	private Integer numOfStuds;
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
	public String getOpenClassInfoId() {
		return openClassInfoId;
	}
	public void setOpenClassInfoId(String openClassInfoId) {
		this.openClassInfoId = openClassInfoId;
	}
	public String getCompFrom() {
		return compFrom;
	}
	public void setCompFrom(String compFrom) {
		this.compFrom = compFrom;
	}
	public String getCompName() {
		return compName;
	}
	public void setCompName(String compName) {
		this.compName = compName;
	}
	public Integer getCompNum() {
		return compNum;
	}
	public void setCompNum(Integer compNum) {
		this.compNum = compNum;
	}
	public Integer getNumOfOpenClasses() {
		return numOfOpenClasses;
	}
	public void setNumOfOpenClasses(Integer numOfOpenClasses) {
		this.numOfOpenClasses = numOfOpenClasses;
	}
	public Integer getNumOfStuds() {
		return numOfStuds;
	}
	public void setNumOfStuds(Integer numOfStuds) {
		this.numOfStuds = numOfStuds;
	}
	
}
