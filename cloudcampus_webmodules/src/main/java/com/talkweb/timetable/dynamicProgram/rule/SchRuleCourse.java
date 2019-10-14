package com.talkweb.timetable.dynamicProgram.rule;

import java.io.Serializable;
import java.util.HashMap;
/**
 * 科目规则
 * @author talkweb
 *
 */
public class SchRuleCourse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3603596535239050147L;
	
	private String courseId;
	private int maxClassNum;
	private int maxAMNum;
	private int maxPMNum;
	/**
	 * 0：尽量上午，1：各节次均可，2:尽量下午
	 */
	private int courseLevel;
	private String gradeId;
	/**
	 * 是否全排上午（目前仅尽量上午使用）
	 */
	private boolean isAll;
	
	private HashMap<Integer,Integer> curAmNum = new HashMap<Integer, Integer>();
	private HashMap<Integer,Integer> curPmNum = new HashMap<Integer, Integer>();
	public String getGradeId() {
		return gradeId;
	}

	public void setGradeId(String gradeId) {
		this.gradeId = gradeId;
	}

	private HashMap<String, SchRulePosition> positions = new HashMap<String, SchRulePosition>();

	/**
	 * 增加科目规则-不拍位置
	 * @param rulePosition
	 */
	public void addPosition(SchRulePosition rulePosition){
		int day = rulePosition.getDay();
		int lesson = rulePosition.getLesson();
		String key = day+","+lesson;
		
		this.positions.put(key,rulePosition);
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

	public HashMap<String, SchRulePosition> getPositions() {
		return positions;
	}

	public void setPositions(HashMap<String, SchRulePosition> positions) {
		this.positions = positions;
	}

	public int getCourseLevel() {
		return courseLevel;
	}

	public void setCourseLevel(int courseLevel) {
		this.courseLevel = courseLevel;
	}
	
	public boolean isAmNumOrPmNumOver(int day,int lesson,int amNum){
		if(maxAMNum!=0&&curAmNum.get(day)!=null&&curAmNum.get(day)>(maxAMNum-1)&&maxAMNum!=0&&lesson<amNum){
			return true;
		}
		if(maxPMNum!=0&&curPmNum.get(day)!=null&&curPmNum.get(day)>(maxPMNum-1)&&maxPMNum!=0&&lesson>=amNum){
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
	/**
	 * 清除单个位置 用于科目规则 最多排课数
	 * @param day
	 * @param lesson
	 * @param amNum
	 */
	public void removeArrangedPosition(int day, int lesson,int amNum) {
		// TODO Auto-generated method stub
		if(lesson<amNum){
			if(curAmNum.containsKey(day)&&curAmNum.get(day)>1){
				curAmNum.put(day, curAmNum.get(day)-1);
			}else{
				if(curAmNum.containsKey(day)){
					curAmNum.remove(day );
				}
			}
		}else{
			if(curPmNum.containsKey(day)&&curPmNum.get(day)>1){
				curPmNum.put(day, curPmNum.get(day)-1);
			}else{
				if(curPmNum.containsKey(day)){
					curPmNum.remove(day );
				}
			}
		}
	}

	/**
	 * 清除所有记录的位置 用于科目规则 最多排课数
	 */
	public void clearArrangedPosition() {
		// TODO Auto-generated method stub
		this.curAmNum = new HashMap<Integer, Integer>();
		this.curPmNum = new HashMap<Integer, Integer>();
	}
	
	/**
	 * 科目是否可排
	 * @param day
	 * @param lesson
	 */
	public boolean canArrangeCourse(int day,int lesson,int amNum){
		SchRulePosition rulePosition = this.positions.get(day+","+lesson);
		if(rulePosition==null||rulePosition.getRuleType()!=0){
			return true&&!isAmNumOrPmNumOver(day, lesson, amNum);
		}else{
			return false;
		}
	}

	public boolean isAll() {
		return isAll;
	}

	public void setAll(boolean isAll) {
		this.isAll = isAll;
	}
}
