package com.talkweb.timetable.dynamicProgram.rule;

import java.io.Serializable;
import java.util.HashMap;

public class SchRuleGround implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1486831969490399473L;

	private String SubjectId;

	private String UsedGradeIds;

	private int MaxClassNum;

	private HashMap<String, Integer> groundClassNum = new HashMap<String, Integer>();

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
	/**
	 * 增加排课 规则计数
	 * @param day
	 * @param lesson
	 */
	public void addArrangedPosition(int day, int lesson) {
		if (groundClassNum.containsKey(day + "," + lesson)) {
			int num = groundClassNum.get(day + "," + lesson);
			groundClassNum.put(day + "," + lesson, num + 1);
		} else {
			groundClassNum.put(day + "," + lesson, 1);
		}
	}
	/**
	 * 减少排课 规则计数
	 * @param day
	 * @param lesson
	 */
	public void removeArrangedPosition(int day, int lesson) {
		if (groundClassNum.containsKey(day + "," + lesson)) {
			int num = groundClassNum.get(day + "," + lesson);
			if(num>1){
				groundClassNum.put(day + "," + lesson, num - 1);
			}else{
				groundClassNum.remove(day + "," + lesson);
			}
		} 
	}

	public boolean isOverRuleGround(int day, int lesson,double arNum) {
		if(arNum<=1){
			arNum = 1;
		}
		if (groundClassNum.containsKey(day + "," + lesson)) {
			int num = groundClassNum.get(day + "," + lesson);
			if (MaxClassNum < num + arNum && MaxClassNum != 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void clearArrangedPosition() {
		// TODO Auto-generated method stub
		this.groundClassNum = new HashMap<String, Integer>();
	}
}
