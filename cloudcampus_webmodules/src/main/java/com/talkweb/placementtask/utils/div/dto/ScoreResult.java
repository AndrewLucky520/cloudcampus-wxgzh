package com.talkweb.placementtask.utils.div.dto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.TreeBasedTable;
import com.talkweb.placementtask.utils.div.Utils;

/**
 * 班级打分结果
 * @author hushowly@foxmail.com
 *
 */
public class ScoreResult {
	
	/**
	 * 分数
	 */
	private Double score;
	 
	
	/**
	 * 行政班集合
	 */
	private List<ClassResult> classResultList = new ArrayList<>();
	
	
	
	/**
	 * 开班剩余数据
	 */
	protected TreeBasedTable<String, String, Integer> remainTable = TreeBasedTable.create();
	
	
	/**
	 * 序列科目开班顺序
	 */
	private LinkedList<String> subjectOrderList = new LinkedList<>();
	
	
	/**
	 * 打印打分结果详细
	 * @param scoreResult
	 */
	public void printScoreInfo(DivContext divContext) {
		
		int adTotalStudentCount = 0;
		
		int adMaxClassSize = Integer.MIN_VALUE;
		int adMinClassSize = Integer.MAX_VALUE;
		
		System.out.println("开班后矩阵剩余情况");
		StringBuffer title1 = new StringBuffer("       ");
		for (Subject  subject : divContext.getSubjectId2SubjectMap().values()) {
			title1.append("  00"+subject.getId());
		}
		System.out.println(title1.toString());
		
		for (Entry<String, Map<String, Integer>> rowEntry : this.remainTable.rowMap().entrySet()) {
			
			String  wishId = Utils.extractWishSubjectGroupId(rowEntry.getKey());
			SubjectGroup wishSubjectGroup = divContext.getWishId2SubjectGroupMap().get(wishId);
			StringBuffer sb = new StringBuffer(wishSubjectGroup.getName()+rowEntry.getKey());
			for (Subject  subject : divContext.getSubjectId2SubjectMap().values()) {
				Integer count = rowEntry.getValue().get(subject.getId());
				
				String studentCountstr = String.valueOf(count);
				int x= 3-studentCountstr.toCharArray().length;
				if(x>0) {
					for(int i=0;i<x;i++) {
						studentCountstr="0"+studentCountstr;
					}
				}
				
				sb.append("  "+studentCountstr);
				
			}
			System.out.println(sb.toString());
		}
		
		System.out.println("开科顺序:"+ this.getSubjectOrderList()+"===================班级:");
		for (ClassResult divClass : this.getClassResultList()) {
			
			TeachClassResult adClassResult = (TeachClassResult)divClass;
			System.out.println(adClassResult.getSubjectId()+" 班级 "+adClassResult.getId()+",总人数:"+adClassResult.getTotalStudent());
			for (Cell cell : adClassResult.getCells()) {
				System.out.println(" "+cell.getRowKey()+"人数:"+cell.getStudentCount());
			}
			if(divClass.getTotalStudent()>adMaxClassSize) adMaxClassSize = divClass.getTotalStudent();
			if(divClass.getTotalStudent()<adMinClassSize) adMinClassSize = divClass.getTotalStudent();
			
			adTotalStudentCount+=divClass.getTotalStudent();
		}
		System.out.println("最大班级:"+adMaxClassSize+"最小班级:"+adMinClassSize);
		System.out.println("总人数："+adTotalStudentCount);
		System.out.println("打分:"+this.getScore()+" 开班数："+this.getClassResultList().size());
	}
	
	public Double getScore() {
		return score;
	}


	public void setScore(Double score) {
		this.score = score;
	}


	public List<ClassResult> getClassResultList() {
		return classResultList;
	}


	public void setClassResultList(List<ClassResult> classResultList) {
		this.classResultList = classResultList;
	}

	public TreeBasedTable<String, String, Integer> getRemainTable() {
		return remainTable;
	}

	public void setRemainTable(TreeBasedTable<String, String, Integer> remainTable) {
		this.remainTable = remainTable;
	}

	public LinkedList<String> getSubjectOrderList() {
		return subjectOrderList;
	}

	public void setSubjectOrderList(LinkedList<String> subjectOrderList) {
		this.subjectOrderList = subjectOrderList;
	}



}
