package com.talkweb.placementtask.utils.div.medium.dto;

import java.util.ArrayList;
import java.util.List;

import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.placementtask.utils.div.dto.SubjectGroup;

public class MediumWishSubjectGroup  extends SubjectGroup{
	
	/**
	 * 分层id
	 */
	private int layId;
	
	/**
	 * 分层名称
	 */
	private String layName;
	
	/**
	 * 志愿固定班开班数
	 */
	private Integer fixedClassCount = 0;
	
	
	/**
	 * 志愿固定开班学生数
	 */
	private Integer fixedStudentCount = 0;

	
	/**
	 * 固定班级人
	 */
	private List<Student> fixedStudents = new ArrayList<>();
	
	
	/**
	 * 志愿走班开级数
	 */
	private Integer goClassCount = 0;
	
	
	/**
	 * 走班班级人
	 */
	private List<Student> goStudents = new ArrayList<>();


	public Integer getFixedClassCount() {
		return fixedClassCount;
	}


	public void setFixedClassCount(Integer fixedClassCount) {
		this.fixedClassCount = fixedClassCount;
	}


	public Integer getFixedStudentCount() {
		return fixedStudentCount;
	}


	public void setFixedStudentCount(Integer fixedStudentCount) {
		this.fixedStudentCount = fixedStudentCount;
	}


	public Integer getGoClassCount() {
		return goClassCount;
	}


	public void setGoClassCount(Integer goClassCount) {
		this.goClassCount = goClassCount;
	}

	
	public Integer getGoStudentCount() {
		return this.getGoStudents().size();
	}

	public List<Student> getFixedStudents() {
		return fixedStudents;
	}


	public void setFixedStudents(List<Student> fixedStudents) {
		this.fixedStudents = fixedStudents;
	}


	public List<Student> getGoStudents() {
		return goStudents;
	}


	public void setGoStudents(List<Student> goStudents) {
		this.goStudents = goStudents;
	}


	public int getLayId() {
		return layId;
	}


	public void setLayId(int layId) {
		this.layId = layId;
	}


	public String getLayName() {
		return layName;
	}


	public void setLayName(String layName) {
		this.layName = layName;
	}


}
