package com.talkweb.placementtask.domain;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.fastjson.JSON;

public class PlacementTask implements Serializable {
	private static final long serialVersionUID = -9172574725173597094L;
	
	private String placementId;
	private Long schoolId;
	private Long autoIncr;
	private Date createDate;
	private Long accountId;
	
	private String placementName;
	private String usedGrade;
	private Integer placementType;
	private Integer status;
	private Integer layerStatus;	// 分层状态，大走班需要2个状态
	
	private Integer genderBalance;
	private Integer differentName;
	private String wfId;
	private String wfTermInfo;
	
	private String termInfo;

	public String getPlacementId() {
		return placementId;
	}

	public void setPlacementId(String placementId) {
		this.placementId = placementId;
	}

	public Long getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(Long schoolId) {
		this.schoolId = schoolId;
	}

	public Long getAutoIncr() {
		return autoIncr;
	}

	public void setAutoIncr(Long autoIncr) {
		this.autoIncr = autoIncr;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public String getPlacementName() {
		return placementName;
	}

	public void setPlacementName(String placementName) {
		this.placementName = placementName;
	}

	public String getUsedGrade() {
		return usedGrade;
	}

	public void setUsedGrade(String usedGrade) {
		this.usedGrade = usedGrade;
	}

	public Integer getPlacementType() {
		return placementType;
	}

	public void setPlacementType(Integer placementType) {
		this.placementType = placementType;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getLayerStatus() {
		return layerStatus;
	}

	public void setLayerStatus(Integer layerStatus) {
		this.layerStatus = layerStatus;
	}

	public Integer getGenderBalance() {
		return genderBalance;
	}

	public void setGenderBalance(Integer genderBalance) {
		this.genderBalance = genderBalance;
	}

	public Integer getDifferentName() {
		return differentName;
	}

	public void setDifferentName(Integer differentName) {
		this.differentName = differentName;
	}

	public String getWfId() {
		return wfId;
	}

	public void setWfId(String wfId) {
		this.wfId = wfId;
	}

	public String getWfTermInfo() {
		return wfTermInfo;
	}

	public void setWfTermInfo(String wfTermInfo) {
		this.wfTermInfo = wfTermInfo;
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
