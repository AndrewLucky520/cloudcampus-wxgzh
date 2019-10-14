package com.talkweb.placementtask.domain;

import java.util.List;

/**
 * @author Administrator
 * 完全以行政班开班的教学班信息(定二走一用)
 */
public class ClassFixedArrange {
	//行政班id
	private String classId;
	//行政班人数
	private Integer AdminNum; 
	//和行政班相关的教学班级(只保存能完全以行政班方式开班的相关教学班,包括选考核学考的)
	private List<TPlDezyClass> TPlDezyClassList;
	//定二科目,也可能是定三或者定一，保存能完全以行政班开班的选考教学班科目List
	private List<String> subjectList;
	//行政班下的志愿组信息 (科目用,分隔)
	private List<String> compFormList;
	
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
	public List<TPlDezyClass> getTPlDezyClassList() {
		return TPlDezyClassList;
	}
	public void setTPlDezyClassList(List<TPlDezyClass> tPlDezyClassList) {
		TPlDezyClassList = tPlDezyClassList;
	}
	public List<String> getSubjectList() {
		return subjectList;
	}
	public void setSubjectList(List<String> subjectList) {
		this.subjectList = subjectList;
	}
	public Integer getAdminNum() {
		return AdminNum;
	}
	public void setAdminNum(Integer adminNum) {
		AdminNum = adminNum;
	}
	public List<String> getCompFormList() {
		return compFormList;
	}
	public void setCompFormList(List<String> compFormList) {
		this.compFormList = compFormList;
	}
	
	
}
