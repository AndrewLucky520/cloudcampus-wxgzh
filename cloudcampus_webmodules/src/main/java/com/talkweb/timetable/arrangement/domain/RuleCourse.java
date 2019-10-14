package com.talkweb.timetable.arrangement.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RuleCourse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3583593665124670385L;
	private String courseId;
	private int maxClassNum;
	private int maxAMNum;
	private int maxPMNum;
	private int courseLevel;
	private String gradeId;
	
	private HashMap<Integer,Integer> curAmNum = new HashMap<Integer, Integer>();
	private HashMap<Integer,Integer> curPmNum = new HashMap<Integer, Integer>();
	public String getGradeId() {
		return gradeId;
	}

	public void setGradeId(String gradeId) {
		this.gradeId = gradeId;
	}

	private List<RulePosition> positions = new ArrayList<RulePosition>();

	public void addPosition(RulePosition rulePosition){
		this.positions.add(rulePosition);
	}
	
	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public int getMaxClassNum() {
		return maxClassNum;
	}

	public void setMaxClassNum(int maxClassNum) {
		this.maxClassNum = maxClassNum;
	}

	public int getMaxAMNum() {
		return maxAMNum;
	}

	public void setMaxAMNum(int maxAMNum) {
		this.maxAMNum = maxAMNum;
	}

	public int getMaxPMNum() {
		return maxPMNum;
	}

	public void setMaxPMNum(int maxPMNum) {
		this.maxPMNum = maxPMNum;
	}

	public List<RulePosition> getPositions() {
		return positions;
	}

	public void setPositions(List<RulePosition> positions) {
		this.positions = positions;
	}

	public int getCourseLevel() {
		return courseLevel;
	}

	public void setCourseLevel(int courseLevel) {
		this.courseLevel = courseLevel;
	}
	
	public boolean isAmNumOrPmNumOver(int day,int lesson,int amNum){
		if(curAmNum.get(day)!=null&&curAmNum.get(day)>(maxAMNum-1)&&maxAMNum!=0&&lesson<amNum){
			return true;
		}
		if(curPmNum.get(day)!=null&&curPmNum.get(day)>(maxPMNum-1)&&maxPMNum!=0&&lesson>=amNum){
			return true;
		}
		return false;
	}

	public void addArrangedPosition(int day, int lesson,int amNum) {
		// TODO Auto-generated method stub
		if(lesson<amNum){
			if(curAmNum.containsKey(day)){
				curAmNum.put(day, curAmNum.get(day)+1);
			}else{
				curAmNum.put(day, 1);
			}
		}else{
			if(curPmNum.containsKey(day)){
				curPmNum.put(day, curPmNum.get(day)+1);
			}else{
				curPmNum.put(day, 1);
			}
		}
	}

	public void clearArrangedPosition() {
		// TODO Auto-generated method stub
		this.curAmNum = new HashMap<Integer, Integer>();
		this.curPmNum = new HashMap<Integer, Integer>();
	}
	
	
}
