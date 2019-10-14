package com.talkweb.placementtask.utils.div.medium.dto;

import java.util.List;

import com.talkweb.placementtask.domain.ClassInfo;


/**
 * 返回中走班分班信息
 * @author hushow
 *
 */
public class MediumClassData {
	
	/**
	 * 固定班级
	 */
	private List<MediumFixedClassInfo> fixedClassList;
	
	/**
	 * 行政班
	 */
	private List<ClassInfo> adClassList;
	
	/**
	 * 教学班
	 */
	private List<ClassInfo> teachClassList;

	
	public List<MediumFixedClassInfo> getFixedClassList() {
		return fixedClassList;
	}
	
	public void setFixedClassList(List<MediumFixedClassInfo> fixedClassList) {
		this.fixedClassList = fixedClassList;
	}

	public List<ClassInfo> getAdClassList() {
		return adClassList;
	}

	public void setAdClassList(List<ClassInfo> adClassList) {
		this.adClassList = adClassList;
	}

	public List<ClassInfo> getTeachClassList() {
		return teachClassList;
	}

	public void setTeachClassList(List<ClassInfo> teachClassList) {
		this.teachClassList = teachClassList;
	}
	
	
	
}
