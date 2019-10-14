package com.talkweb.timetable.arrangement.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RuleGround implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7027674090773465437L;

	private String SubjectId;
	
	private String UsedGradeIds;
	
	private int  MaxClassNum;

	private HashMap<String,Double> groundClassNum = new HashMap<String, Double>();
	
	/**
	 * 场地规则 单双周课时计算
	 */
	private HashMap<String,List<Integer>> groundClassLessonTypes = new HashMap<String, List<Integer>>();
	
	public String getSubjectId() {
		return SubjectId;
	}

	public void setSubjectId(String subjectId) {
		SubjectId = subjectId;
	}

	public String getUsedGradeIds() {
		return UsedGradeIds;
	}

	public void setUsedGradeIds(String usedGradeIds) {
		UsedGradeIds = usedGradeIds;
	}

	public int getMaxClassNum() {
		return MaxClassNum;
	}

	public void setMaxClassNum(int maxClassNum) {
		MaxClassNum = maxClassNum;
	}
	
	public void addArrangedPosition(int day,int lesson,int courseType){
		List<Integer> list =  new ArrayList<Integer>();
		if(groundClassLessonTypes.containsKey(day+","+lesson)){
			list = groundClassLessonTypes.get(day+","+lesson);
		} 
		list.add(courseType);
		groundClassLessonTypes.put(day+","+lesson, list);
	}
	
	public boolean isOverRuleGround(int day,int lesson,int srcday,int courseType){
		if(groundClassLessonTypes.containsKey(day+","+lesson)){
			List<Integer> list = new ArrayList<Integer>();
			list.addAll(groundClassLessonTypes.get(day+","+lesson));
			list.add(courseType);
			double sum = 0;
			List<Integer> l1 = new ArrayList<Integer>();
			List<Integer> l2 = new ArrayList<Integer>();
			for(int ct :list ){
				if(ct ==0){
					sum++;
				}else if(ct==1){
					l1 .add(ct);
				}else if(ct==2){
					l2 .add(ct);
				}
			}
			if(l1.size()==l2.size()){
				sum += l1.size();
			}else if(l1.size()>l2.size()){
				sum += l2.size();
				sum += (l1.size()-l2.size());
			}else if(l1.size()<l2.size()){
				sum += l1.size();
				sum += (l2.size()-l1.size());
			}
			if(sum>MaxClassNum){
				return true;
			}
			return false;
			 
		}else{
			return false;
		}
	}

	public void clearArrangedPosition() {
		// TODO Auto-generated method stub
		this.groundClassNum = new HashMap<String, Double>();
		this.groundClassLessonTypes = new HashMap<String, List<Integer>>();
	}

	public HashMap<String, List<Integer>> getGroundClassLessonTypes() {
		return groundClassLessonTypes;
	}

	public void setGroundClassLessonTypes(
			HashMap<String, List<Integer>> groundClassLessonTypes) {
		this.groundClassLessonTypes = groundClassLessonTypes;
	}
}
