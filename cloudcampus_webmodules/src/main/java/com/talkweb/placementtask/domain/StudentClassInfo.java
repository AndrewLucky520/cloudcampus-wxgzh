package com.talkweb.placementtask.domain;

import java.util.ArrayList;
import java.util.List;
/**
 * 保存学生对应的班级信息
 * */
public class StudentClassInfo {
	private String schoolId;
	private String placementId;
	private String accountId;
	/*行政班id，大走班下取基础数据里面的班级id*/
	private String classId;
	

	private List<TPlDezyClass> tPlDezyClasss = new ArrayList<TPlDezyClass>();
	public String getSchoolId() {
		return schoolId;
	}
	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}
	public String getPlacementId() {
		return placementId;
	}
	public void setPlacementId(String placementId) {
		this.placementId = placementId;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	
	public List<TPlDezyClass> getTPlDezyClasss(){
		return tPlDezyClasss;
	}
	
	public void addTPlDezyClasss(TPlDezyClass tPlDezyClass){
		tPlDezyClasss.add(tPlDezyClass);
	}
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
	
}
