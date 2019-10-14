package com.talkweb.placementtask.domain;

import java.util.List;

/**
 * @author Administrator
 * 保存subjectCompId与班级的关联关系
 */
public class SubjectcompToClassInfo {
	private String subjectCompId;
	private Integer compNum;
	//行政班id(行政班信息不放到TPlDezyClass对象是因为要兼容大走班的行政班信息)
	private String AdminClassId;
	//行政班人数(大走班模式下为空)
	private Integer AdminClassNum;
	//教学班实体类List
	private List<TPlDezyClass> tPlDezyClassList;
	//班级Key(班级id用“,”分隔，按照行政班、政史地物化生的顺序)
	private String classKey;
	
	
	
	public String getSubjectCompId() {
		return subjectCompId;
	}
	public void setSubjectCompId(String subjectCompId) {
		this.subjectCompId = subjectCompId;
	}
	public Integer getCompNum() {
		return compNum;
	}
	public void setCompNum(Integer compNum) {
		this.compNum = compNum;
	}
	public String getAdminClassId() {
		return AdminClassId;
	}
	public void setAdminClassId(String adminClassId) {
		AdminClassId = adminClassId;
	}
	public Integer getAdminClassNum() {
		return AdminClassNum;
	}
	public void setAdminClassNum(Integer adminClassNum) {
		AdminClassNum = adminClassNum;
	}
	public List<TPlDezyClass> gettPlDezyClassList() {
		return tPlDezyClassList;
	}
	public void settPlDezyClassList(List<TPlDezyClass> tPlDezyClassList) {
		this.tPlDezyClassList = tPlDezyClassList;
	}
	public String getClassKey() {
		return classKey;
	}
	public void setClassKey(String classKey) {
		this.classKey = classKey;
	}
	
}
