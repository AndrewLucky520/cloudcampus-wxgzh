package com.talkweb.placementtask.domain;

import java.util.List;

/**
 * 分班完成后，保存班级信息以及相关学生信息
 * */
public class ClassInfo {
	//班级名
	private String tclassName;
	//班级id
	private String tclassId;
	//班级序列(大走班是1-3，定二走一是0-6)，7选3模式下会到4和7
	private Integer classSeq;
	//学选类型(行政班0,选考班1,学考班2)
	private Integer tclassLevel;
	//班级类型(行政班6、教学班7)
	private Integer classInfo;
	//场地id
	private String groundId;
	//场地名
	private String groundName;
	//科目id(定二走一的行政班为-999)
	private String subjectId;
	//班级人数
	private Integer tclassNum;
	//学生列表实体类列表(id、name、wishId)
	private List<StudentbaseInfo> studentLists;
	 
	//定二走一情况下行政班定二科目(按科目id排序)
	private String[] fixedSubjectIds;
	
	public String getTclassName() {
		return tclassName;
	}
	public void setTclassName(String tclassName) {
		this.tclassName = tclassName;
	}
	public String getTclassId() {
		return tclassId;
	}
	public void setTclassId(String tclassId) {
		this.tclassId = tclassId;
	}
	public Integer getClassSeq() {
		return classSeq;
	}
	public void setClassSeq(Integer classSeq) {
		this.classSeq = classSeq;
	}
	public Integer getTclassLevel() {
		return tclassLevel;
	}
	public void setTclassLevel(Integer tclassLevel) {
		this.tclassLevel = tclassLevel;
	}
	public String getGroundId() {
		return groundId;
	}
	public void setGroundId(String groundId) {
		this.groundId = groundId;
	}
	public String getGroundName() {
		return groundName;
	}
	public void setGroundName(String groundName) {
		this.groundName = groundName;
	}
	public String getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
	public Integer getTclassNum() {
		return tclassNum;
	}
	public void setTclassNum(Integer tclassNum) {
		this.tclassNum = tclassNum;
	}
	public Integer getClassInfo() {
		return classInfo;
	}
	public void setClassInfo(Integer classInfo) {
		this.classInfo = classInfo;
	}
	public List<StudentbaseInfo> getStudentLists() {
		return studentLists;
	}
	public void setStudentLists(List<StudentbaseInfo> studentLists) {
		this.studentLists = studentLists;
	}
	public String[] getFixedSubjectIds() {
		return fixedSubjectIds;
	}
	public void setFixedSubjectIds(String[] fixedSubjectIds) {
		this.fixedSubjectIds = fixedSubjectIds;
	}
	

}
