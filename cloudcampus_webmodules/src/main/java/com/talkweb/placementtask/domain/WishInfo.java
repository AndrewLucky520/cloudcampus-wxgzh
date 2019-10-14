package com.talkweb.placementtask.domain;

import java.util.List;

/**
 * @author Administrator
 * 大走班分班用，保存组合下学生信息
 */
public class WishInfo {
	//组合信息(比如说政史地就是456)
	private String wishzhId;
	//组合所对应志愿
	private List<String> wishSubjectList;
	//组合学生信息
	private List<StudentbaseInfo> StudentList;
	public String getWishzhId() {
		return wishzhId;
	}
	public void setWishzhId(String wishzhId) {
		this.wishzhId = wishzhId;
	}
	public List<String> getWishSubjectList() {
		return wishSubjectList;
	}
	public void setWishSubjectList(List<String> wishSubjectList) {
		this.wishSubjectList = wishSubjectList;
	}
	public List<StudentbaseInfo> getStudentList() {
		return StudentList;
	}
	public void setStudentList(List<StudentbaseInfo> studentList) {
		StudentList = studentList;
	}
	
}
