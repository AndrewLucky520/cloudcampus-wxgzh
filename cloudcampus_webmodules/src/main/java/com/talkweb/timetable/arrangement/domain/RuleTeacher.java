package com.talkweb.timetable.arrangement.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RuleTeacher implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1351280905327320053L;
	private String teacherId;
	private int maxPerDay;
	
	private Map<Integer,Double> dayNum =new HashMap<Integer, Double>();
	/**
	 * 课程上课类型
	 */
	private Map<Integer,List<Integer>> dayLessonTypes =new HashMap<Integer, List<Integer>>();
	
	private List<RulePosition> positions = new ArrayList<RulePosition>();

	public void addPosition(RulePosition rulePosition){
		this.positions.add(rulePosition);
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

	public List<RulePosition> getPositions() {
		return positions;
	}

	public void setPositions(List<RulePosition> positions) {
		this.positions = positions;
	}
	
	public boolean isTeacherOverNum(int day, int srcday,int courseType){
		if(this.dayLessonTypes.containsKey(day)&&maxPerDay!=0){
					
			if(srcday!=-1&&day==srcday ){
				return false;
			}else{
				List<Integer> list = new ArrayList<Integer>();
				list.addAll(dayLessonTypes.get(day));
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
				if(sum>maxPerDay){
					return true;
				}
				return false;
			}
			 
		}else{
			return false;
		}
	}
	
	public void addArrangedPosition(int day,int cType){
		List<Integer> lessons = new ArrayList<Integer>();
		if(this.dayLessonTypes.containsKey(day)){
			lessons = dayLessonTypes.get(day);
		}
		lessons.add(cType);
		dayLessonTypes.put(day, lessons);
	}

	public void clearArrangedPosition() {
		// TODO Auto-generated method stub
		this.dayNum = new HashMap<Integer, Double>();
		this.dayLessonTypes = new HashMap<Integer, List<Integer>>();
	}

	public Map<Integer, List<Integer>> getDayLessonTypes() {
		return dayLessonTypes;
	}

	public void setDayLessonTypes(Map<Integer, List<Integer>> dayLessonTypes) {
		this.dayLessonTypes = dayLessonTypes;
	}
}
