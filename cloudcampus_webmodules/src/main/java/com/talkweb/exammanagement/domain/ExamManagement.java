package com.talkweb.exammanagement.domain;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.fastjson.JSONObject;

public class ExamManagement implements Serializable {
	private static final long serialVersionUID = -7433195371094821597L;
	
	private String examManagementId;
	private Long schoolId;
	private Integer autoIncr;
	private String name;
	private Date createDateTime;
	private Long accountId;
	private Integer status = 0;	// 状态默认为0
	private String termInfo;

	private Long serialNumber;
	public String getExamManagementId() {
		return examManagementId;
	}

	public void setExamManagementId(String examManagementId) {
		this.examManagementId = examManagementId;
	}

	public Long getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(Long schoolId) {
		this.schoolId = schoolId;
	}

	public Integer getAutoIncr() {
		return autoIncr;
	}

	public void setAutoIncr(Integer autoIncr) {
		this.autoIncr = autoIncr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}
	
	public Long getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(Long serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String toString(){
		return JSONObject.toJSONString(this);
	}
}
