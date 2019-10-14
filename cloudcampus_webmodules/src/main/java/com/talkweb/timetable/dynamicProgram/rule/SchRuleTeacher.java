package com.talkweb.timetable.dynamicProgram.rule;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SchRuleTeacher implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1812967028787772569L;

	private String teacherId;
	/**
	 *  每天最大上课数
	 */
	private int maxPerDay;
	/**
	 *  日期-每天最大上课数映射
	 */
	
	private Map<Integer,Integer> dayNum =new HashMap<Integer, Integer>();
	
	/**
	 *  位置规则
	 */
	private HashMap<String,SchRulePosition> positions = new HashMap<String, SchRulePosition>();

	public HashMap<String, SchRulePosition> getPositions() {
		return positions;
	}

	public void addPosition(SchRulePosition rulePosition){
		
		int pday = rulePosition.getDay();
		int pLesson = rulePosition.getLesson();
		
		this.positions.put(pday+","+pLesson,rulePosition);
	}
	
	public String getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(String teacherId) {
		this.teacherId = teacherId;
	}

	public int getMaxPerDay() {
		return maxPerDay;
	}

	public void setMaxPerDay(int maxPerDay) {
		this.maxPerDay = maxPerDay;
	}
	/**
	 * 教师位置规则是否满足
	 * @param day
	 * @param lesson
	 * @param srcDay 
	 * @return
	 */
	public boolean canArrangeTeacherPositions(int day,int lesson,double arNum) {
		SchRulePosition rulePosition = this.positions.get(day+","+lesson);
		if(rulePosition==null||rulePosition.getRuleType()!=0){
			return true&&!isTeacherOverNum(day, arNum);
		}else{
			return false;
		}
	}

	public void setPositions(List<SchRulePosition> positions) {
		for(SchRulePosition rulePosition:positions){
			addPosition(rulePosition);
		}
	}
	
	/**
	 * 用于判断教师规则是否合法--每天最大上课数
	 * @param srcDay 
	 */
	public boolean isTeacherOverNum(int day,double arNum ){
		if(arNum<=1){
			arNum  = 1;
		}
		if(this.dayNum.containsKey(day)){
			if(maxPerDay!=0){
				if(this.dayNum.get(day)+arNum>maxPerDay){
					return true;
				}else{
					//当天老师自己换课
					
					return false;
				}
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	/**
	 * 记录 教师每天位置
	 */
	public void addArrangedPosition(int day){
		if(this.dayNum.containsKey(day)){
			dayNum.put(day, dayNum.get(day)+1);
		}else{
			dayNum.put(day,  1);
		}
	}
	/**
	 * 清除单条记录的教师每天位置
	 */
	public void removeArrangedPosition(int day){
		if(this.dayNum.containsKey(day)){
			Integer num = dayNum.get(day);
			if(num>=1){
				dayNum.put(day, num-1);
			}else{
				dayNum.remove(day);
			}
		} 
	}

	/**
	 * 清除记录的教师每天位置
	 */
	public void clearArrangedPosition() {
		// TODO Auto-generated method stub
		this.dayNum = new HashMap<Integer, Integer>();
	}
}
