package com.talkweb.discipline.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class DisciplineDetail implements Serializable{
 
	private static final long serialVersionUID = 3428679946266625440L;

	private String disciplineId;
	private String schoolId;
	private String classId;
	private Integer  rowNum;
	private List<String> disciComponentIds = new ArrayList<String>();
	private List<String> scores = new ArrayList<String>();
	private List<JSONObject> errorlist=new ArrayList<JSONObject>();
 
	public String getDisciplineId() {
		return disciplineId;
	}
	public void setDisciplineId(String disciplineId) {
		this.disciplineId = disciplineId;
	}
	
	public String getSchoolId() {
		return schoolId;
	}
	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}
 
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
 
	public List<JSONObject> getErrorlist() {
		return errorlist;
	}
	public void setErrorlist(List<JSONObject> errorlist) {
		this.errorlist = errorlist;
	}
	
 
	public List<String> getScores() {
		return scores;
	}
	public void setScores(List<String> scores) {
		this.scores = scores;
	}
	
 
	public List<String> getDisciComponentIds() {
		return disciComponentIds;
	}
	public void setDisciComponentIds(List<String> disciComponentIds) {
		this.disciComponentIds = disciComponentIds;
	}
 
	public Integer getRowNum() {
		return rowNum;
	}
	public void setRowNum(Integer rowNum) {
		this.rowNum = rowNum;
	}
	
	@Override
	public String toString() {
		return "DisciplineDetail [disciComponentIds=" + disciComponentIds + ", scores="
				+ scores + " , rowNum =" + rowNum + "]";
	}
	
 
}
