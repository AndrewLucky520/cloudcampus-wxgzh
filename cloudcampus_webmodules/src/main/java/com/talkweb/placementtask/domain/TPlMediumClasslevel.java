package com.talkweb.placementtask.domain;

/**
 * 中走班走班组合分层设置
 * 对应t_pl_medium_classlevel实体类
 */
public class TPlMediumClasslevel {
	private String placementId;
	private String schoolId;
	private String termInfo;
	private String compFrom;
	private String subLevelName;
	private Integer subLevel;
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
	public String getCompFrom() {
		return compFrom;
	}
	public void setCompFrom(String compFrom) {
		this.compFrom = compFrom;
	}
	public String getSubLevelName() {
		return subLevelName;
	}
	public void setSubLevelName(String subLevelName) {
		this.subLevelName = subLevelName;
	}
	public Integer getSubLevel() {
		return subLevel;
	}
	public void setSubLevel(Integer subLevel) {
		this.subLevel = subLevel;
	}
	

}
