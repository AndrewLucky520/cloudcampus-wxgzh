package com.talkweb.placementtask.utils.div.dto;

import com.talkweb.placementtask.utils.div.dto.Subject.UP_OR_DOWN;

public class SubjectAvgSize {
	
	
	private String subjectId;
	
	private Integer totalStudentCount;
	
	private Double classCount;

	private Double classCountMod;
	
	private Double avgSize;
	
	private UP_OR_DOWN upOrDown;
	
	public SubjectAvgSize copyFrom(SubjectAvgSize sas) {
		this.setSubjectId(sas.getSubjectId());
		this.setTotalStudentCount(sas.getTotalStudentCount());
		this.setClassCount(sas.getClassCount());
		this.setClassCountMod(sas.getClassCountMod());
		this.setAvgSize(sas.getAvgSize());
		this.setUpOrDown(sas.getUpOrDown());
		return this;
	}


	public String getSubjectId() {
		return subjectId;
	}


	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}


	public Integer getTotalStudentCount() {
		return totalStudentCount;
	}


	public void setTotalStudentCount(Integer totalStudentCount) {
		this.totalStudentCount = totalStudentCount;
	}


	public Double getClassCount() {
		return classCount;
	}


	public void setClassCount(Double classCount) {
		this.classCount = classCount;
	}


	public Double getClassCountMod() {
		return classCountMod;
	}


	public void setClassCountMod(Double classCountMod) {
		this.classCountMod = classCountMod;
	}


	public Double getAvgSize() {
		return avgSize;
	}


	public void setAvgSize(Double avgSize) {
		this.avgSize = avgSize;
	}


	public UP_OR_DOWN getUpOrDown() {
		return upOrDown;
	}


	public void setUpOrDown(UP_OR_DOWN upOrDown) {
		this.upOrDown = upOrDown;
	}
}
