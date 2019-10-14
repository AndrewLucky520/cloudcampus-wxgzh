package com.talkweb.placementtask.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;

public class OpenClassTask implements Serializable{
	private static final long serialVersionUID = -2839111178569319890L;
	
	private String openClassTaskId;
	private Long schoolId;
	private String placementId;
	private String openClassInfoId;
	private Integer status;
	
	private Integer subjectLevel;
	private String layName;
	private Integer layValue;
	
	private Integer numOfStuds;
	private Integer numOfOpenClasses;
	private Integer classSize;
	
	private String termInfo;
	
	private OpenClassInfo openClassInfo;
	
	private List<TeachingClassInfo> teachingClassInfos = new ArrayList<TeachingClassInfo>();

	public String getOpenClassTaskId() {
		return openClassTaskId;
	}

	public void setOpenClassTaskId(String openClassTaskId) {
		this.openClassTaskId = openClassTaskId;
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

	public String getOpenClassInfoId() {
		return openClassInfoId;
	}

	public void setOpenClassInfoId(String openClassInfoId) {
		this.openClassInfoId = openClassInfoId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getSubjectLevel() {
		return subjectLevel;
	}

	public void setSubjectLevel(Integer subjectLevel) {
		this.subjectLevel = subjectLevel;
	}

	public String getLayName() {
		return layName;
	}

	public void setLayName(String layName) {
		this.layName = layName;
	}

	public Integer getLayValue() {
		return layValue;
	}

	public void setLayValue(Integer layValue) {
		this.layValue = layValue;
	}

	public Integer getNumOfStuds() {
		return numOfStuds;
	}

	public void setNumOfStuds(Integer numOfStuds) {
		this.numOfStuds = numOfStuds;
	}

	public Integer getNumOfOpenClasses() {
		return numOfOpenClasses;
	}

	public void setNumOfOpenClasses(Integer numOfOpenClasses) {
		this.numOfOpenClasses = numOfOpenClasses;
	}

	public Integer getClassSize() {
		return classSize;
	}

	public void setClassSize(Integer classSize) {
		this.classSize = classSize;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}

	public OpenClassInfo getOpenClassInfo() {
		return openClassInfo;
	}

	public void setOpenClassInfo(OpenClassInfo openClassInfo) {
		this.openClassInfo = openClassInfo;
	}
	
	public List<TeachingClassInfo> getTeachingClassInfos() {
		return teachingClassInfos;
	}

	@Override
	public String toString(){
		return JSON.toJSONString(this);
	}
	
	public void addTeachingClassInfo(TeachingClassInfo tClassInfo){
		this.teachingClassInfos.add(tClassInfo);
	}
}
