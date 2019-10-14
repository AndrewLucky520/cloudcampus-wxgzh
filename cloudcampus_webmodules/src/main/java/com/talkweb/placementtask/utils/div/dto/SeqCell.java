package com.talkweb.placementtask.utils.div.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 科目序列单元格
 * @author hushowly@foxmail.com
 *
 */
public class SeqCell {
	
	/**
	 * 所属科目
	 */
	private Subject subject;
	
	/**
	 * 所属序列
	 */
	private Integer seqId;
	
	
	/**
	 * 需分配学生总人数
	 */
	private int studentTotalCount;
	
	/**
	 * 当前格子需分班数
	 */
	private int classCount;
	
	/**
	 * 班级学生人数
	 */
	private int classSize;
	
	
	/**
	 * 当前分配学生人数
	 */
	private int currentStudentCount;
	
	/**
	 * 是否开完班
	 */
	private Boolean isDiv = false;
	
	/**
	 * 序列上开班信息
	 */
	private List<ClassResult> classResultList = new ArrayList<ClassResult>();
	
	public SeqCell(Integer seqId, Subject subject) {
		this.seqId = seqId;
		this.subject = subject;
	}

	
	public SeqCell(Integer seqId, String subjectId, String subjectName) {
		this.seqId = seqId;
		this.subject = new Subject(subjectId, subjectName);
	}

	public SeqCell(Integer seqId, Subject subject, int classCount) {
		this.seqId = seqId;
		this.subject = subject;
		this.classCount =classCount;
	}
	
	
	public Subject getSubject() {
		return subject;
	}


	public void setSubject(Subject subject) {
		this.subject = subject;
	}


	public Integer getSeqId() {
		return seqId;
	}


	public void setSeqId(Integer seqId) {
		this.seqId = seqId;
	}


	public int getStudentTotalCount() {
		return studentTotalCount;
	}


	public void setStudentTotalCount(int studentTotalCount) {
		this.studentTotalCount = studentTotalCount;
	}


	public int getClassCount() {
		return classCount;
	}


	public void setClassCount(int classCount) {
		this.classCount = classCount;
	}


	public int getClassSize() {
		return classSize;
	}


	public void setClassSize(int classSize) {
		this.classSize = classSize;
	}


	public int getCurrentStudentCount() {
		return currentStudentCount;
	}


	public void setCurrentStudentCount(int currentStudentCount) {
		this.currentStudentCount = currentStudentCount;
	}


	public List<ClassResult> getClassResultList() {
		return classResultList;
	}


	public void setClassResultList(List<ClassResult> classResultList) {
		this.classResultList = classResultList;
	}


	public Boolean getIsDiv() {
		return isDiv;
	}


	public void setIsDiv(Boolean isDiv) {
		this.isDiv = isDiv;
	}

}
