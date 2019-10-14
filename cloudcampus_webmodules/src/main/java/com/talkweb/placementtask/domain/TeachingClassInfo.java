package com.talkweb.placementtask.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;

public class TeachingClassInfo implements Serializable {
	private static final long serialVersionUID = 745341933604709203L;

	private String teachingClassId;
	private Long schoolId;
	private String usedGrade;
	private String teachingClassName;
	private String placementId;
	private String openClassInfoId = "";
	private String openClassTaskId = "";
	private String termInfo;

	private int numOfBoys = 0;
	private int numOfGirls = 0;
	private int numOfStuds = 0;

	private OpenClassTask openClassTask;
	
	private List<StudentInfo> studInfos = new ArrayList<StudentInfo>();
	
	private Set<String> studNameSet = new HashSet<String>();

	public String getTeachingClassId() {
		return teachingClassId;
	}

	public void setTeachingClassId(String teachingClassId) {
		this.teachingClassId = teachingClassId;
	}

	public Long getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(Long schoolId) {
		this.schoolId = schoolId;
	}

	public String getUsedGrade() {
		return usedGrade;
	}

	public void setUsedGrade(String usedGrade) {
		this.usedGrade = usedGrade;
	}

	public String getTeachingClassName() {
		return teachingClassName;
	}

	public void setTeachingClassName(String teachingClassName) {
		this.teachingClassName = teachingClassName;
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

	public String getOpenClassTaskId() {
		return openClassTaskId;
	}

	public void setOpenClassTaskId(String openClassTaskId) {
		this.openClassTaskId = openClassTaskId;
	}

	public String getTermInfo() {
		return termInfo;
	}

	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}

	public int getNumOfBoys() {
		return numOfBoys;
	}

	public void setNumOfBoys(int numOfBoys) {
		this.numOfBoys = numOfBoys;
	}

	public int getNumOfGirls() {
		return numOfGirls;
	}

	public void setNumOfGirls(int numOfGirls) {
		this.numOfGirls = numOfGirls;
	}

	public int getNumOfStuds() {
		return numOfStuds;
	}

	public void setNumOfStuds(int numOfStuds) {
		this.numOfStuds = numOfStuds;
	}

	public OpenClassTask getOpenClassTask() {
		return openClassTask;
	}

	public void setOpenClassTask(OpenClassTask openClassTask) {
		this.openClassTask = openClassTask;
	}

	public List<StudentInfo> getStudInfos() {
		return studInfos;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	
	public void incrNumOfBoys(){
		this.numOfBoys ++;
	}
	
	public void incrNumOfGirls(){
		this.numOfGirls ++;
	}
	
	public void incrNumOfStuds(){
		this.numOfStuds ++;
	}
	
	public void addStudNames(String studName) {
		this.studNameSet.add(studName);
	}

	public boolean hasStudName(String studName) {
		return this.studNameSet.contains(studName);
	}
	
	public void addStudentInfo(StudentInfo studInfo){
		this.studInfos.add(studInfo);
	}
}
