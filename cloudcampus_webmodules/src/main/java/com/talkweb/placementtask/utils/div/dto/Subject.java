package com.talkweb.placementtask.utils.div.dto;

import java.math.BigDecimal;
import java.util.Map;

import com.google.common.collect.Table;

public class Subject {
	
	public enum UP_OR_DOWN{
		UP,
		DOWN,
		ROUND
	}
	
	/**
	 * 科目id
	 */
	private String id;
	
	/**
	 * 科目名称
	 */
	private String name;
	
	/**
	 * 选择此科目的学生总数
	 */
	private int totalStudentCount = 0;
	
	/**
	 * 科目开班数 = totalStudentCount/globalAgvClassSize  
	 * 具体参考最大班额向上取整或向下取整
	 */
	private int classTotalCount = 0;
	
	/**
	 * 本科目下平均班额=totalStudentCount/classTotalCount
	 */
	private BigDecimal avgClassSize = new BigDecimal(0);
	
	
	/**
	 * 行政班选一选二总班级数
	 */
	private int adClassTotalCount = 0;
	
	
	/**
	 * 学考科目下总班级数
	 */
	private int learnClassTotalCount = 0;
	
	/**
	 * 学考科目下总学生数
	 */
	private int learnStudentTotalCount = 0;
	
	/**
	 * 学考科目平均值
	 */
	private BigDecimal learnAvgClassSize = new BigDecimal(0);
	
	
	private UP_OR_DOWN upOrDown;

	
	public static void printAdClasshours(Map<String, Subject> subjectMap, Table<Integer, String, SeqCell> classHourTable) {
		
		StringBuffer bestTitle = new StringBuffer("         ");
		StringBuffer classTotalCountStr = new StringBuffer("总开班数               ");
		StringBuffer classTotalCountStr2 = new StringBuffer("选一选二最优       ");
		StringBuffer adClassTotalCountStr = new StringBuffer("选一选二行政       ");
		for (Subject subject : subjectMap.values()) {
			bestTitle.append("  "+subject.getId());
			int a = classHourTable.column(subject.getId()).get(1).getClassCount()+classHourTable.column(subject.getId()).get(2).getClassCount();
			classTotalCountStr2.append("  "+a);
			classTotalCountStr.append("  "+subject.getClassTotalCount());
			adClassTotalCountStr.append("  "+subject.getAdClassTotalCount());
		}
		System.out.println(bestTitle);
		System.out.println(classTotalCountStr);
		System.out.println(classTotalCountStr2);
		System.out.println(adClassTotalCountStr);
	}
	
	
	public Subject(String id, String name) {
		this.id = id;
		this.name =name;
	}
	
	public Subject(String id, String name, int classTotalCount) {
		this.id = id;
		this.name =name;
		this.classTotalCount = classTotalCount;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTotalStudentCount() {
		return totalStudentCount;
	}

	public void setTotalStudentCount(int totalStudentCount) {
		this.totalStudentCount = totalStudentCount;
	}

	public int getClassTotalCount() {
		return classTotalCount;
	}

	public void setClassTotalCount(int classTotalCount) {
		this.classTotalCount = classTotalCount;
	}

	public BigDecimal getAvgClassSize() {
		return avgClassSize;
	}
	
	public void setAvgClassSize(BigDecimal avgClassSize) {
		this.avgClassSize = avgClassSize;
	}

	public int getAdClassTotalCount() {
		return adClassTotalCount;
	}

	public void setAdClassTotalCount(int adClassTotalCount) {
		this.adClassTotalCount = adClassTotalCount;
	}



	public int getLearnClassTotalCount() {
		return learnClassTotalCount;
	}



	public void setLearnClassTotalCount(int learnClassTotalCount) {
		this.learnClassTotalCount = learnClassTotalCount;
	}



	public int getLearnStudentTotalCount() {
		return learnStudentTotalCount;
	}



	public void setLearnStudentTotalCount(int learnStudentTotalCount) {
		this.learnStudentTotalCount = learnStudentTotalCount;
	}


	public BigDecimal getLearnAvgClassSize() {
		return learnAvgClassSize;
	}


	public void setLearnAvgClassSize(BigDecimal learnAvgClassSize) {
		this.learnAvgClassSize = learnAvgClassSize;
	}

	
	public UP_OR_DOWN getUpOrDown() {
		return upOrDown;
	}


	public void setUpOrDown(UP_OR_DOWN upOrDown) {
		this.upOrDown = upOrDown;
	}
	
	
}
