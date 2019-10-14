package com.talkweb.timetable.dynamicProgram.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.talkweb.timetable.dynamicProgram.enums.CourseLevel;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleCourse;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleTeacher;
import com.talkweb.timetable.dynamicProgram.rule.ScheduleRule;

public class ScheduleTaskGroup implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -944310105640777320L;

	/**
	 * 系统分配分组排课的id
	 */
	private String taskGroupId;
	private String gradeId;
	private String gradeLevel;
	private List<String> teacherIds;

	private List<ScheduleTask> childTasks;
	
	private String courseId;
	/**
	 * 课程重要程度
	 */
	private CourseLevel courseLevel;
	
	private double avgTaskNum;
	
	private double groupTaskNum;
	
	private int maxDays;
	
	private boolean halfAtLast = false;
	/**
	 * 组内最大连拍数
	 */
	private int groupMaxSpNum;
	
	private int hasArrangeSpNum;
	public int getHasArrangeSpNum() {
		return hasArrangeSpNum;
	}

	public void setHasArrangeSpNum(int hasArrangeSpNum) {
		this.hasArrangeSpNum = hasArrangeSpNum;
	}

	private int classAmNum;
	
	private int classPmNum;
	
	private boolean isNeedCourseNumControl ;
	
	private boolean isFinish = false;
	
	private String groupDays ;
	
	/**
	 * 科目排序
	 */
	private int courseOrder;
	
	
	private int currentLc = 0;
	
	private HashMap<Integer,List<String>> lcPositionsMap = new HashMap<Integer, List<String>>(); 

	public int getCurrentLc() {
		return currentLc;
	}

	public void setCurrentLc(int currentLc) {
		this.currentLc = currentLc;
	}

	public String getTaskGroupId() {
		return taskGroupId;
	}

	public void setTaskGroupId(String taskGroupId) {
		this.taskGroupId = taskGroupId;
	}

	public String getGradeId() {
		return gradeId;
	}

	public void setGradeId(String gradeId) {
		this.gradeId = gradeId;
	}

	public String getGradeLevel() {
		return gradeLevel;
	}

	public void setGradeLevel(String gradeLevel) {
		this.gradeLevel = gradeLevel;
	}

	public List<String> getTeacherIds() {
		return teacherIds;
	}

	public void setTeacherIds(List<String> teacherIds) {
		this.teacherIds = teacherIds;
	}

	public List<ScheduleTask> getChildTasks() {
		return childTasks;
	}

	public void setChildTasks(List<ScheduleTask> childTasks) {
		this.childTasks = childTasks;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public CourseLevel getCourseLevel() {
		return courseLevel;
	}

	public void setCourseLevel(CourseLevel courseLevel) {
		this.courseLevel = courseLevel;
	}

	public double getAvgTaskNum() {
		return avgTaskNum;
	}

	public void setAvgTaskNum(double avgTaskNum) {
		this.avgTaskNum = avgTaskNum;
	}

	public double getGroupTaskNum() {
		return groupTaskNum;
	}

	public void setGroupTaskNum(double groupTaskNum) {
		this.groupTaskNum = groupTaskNum;
	}

	public int getMaxDays() {
		return maxDays;
	}

	public void setMaxDays(int maxDays) {
		this.maxDays = maxDays;
	}

	public boolean isHalfAtLast() {
		return halfAtLast;
	}

	public void setHalfAtLast(boolean halfAtLast) {
		this.halfAtLast = halfAtLast;
	}

	public int getGroupMaxSpNum() {
		return groupMaxSpNum;
	}

	public void setGroupMaxSpNum(int groupMaxSpNum) {
		this.groupMaxSpNum = groupMaxSpNum;
	}

	public int getClassAmNum() {
		return classAmNum;
	}

	public void setClassAmNum(int classAmNum) {
		this.classAmNum = classAmNum;
	}

	public double getClassPmNum() {
		return classPmNum;
	}

	public void setClassPmNum(int classPmNum) {
		this.classPmNum = classPmNum;
	}

	public boolean isNeedCourseNumControl() {
		return isNeedCourseNumControl;
	}

	public void setNeedCourseNumControl(boolean isNeedCourseNumControl) {
		this.isNeedCourseNumControl = isNeedCourseNumControl;
	}


	public int getCourseOrder() {
		return courseOrder;
	}

	public void setCourseOrder(int courseOrder) {
		this.courseOrder = courseOrder;
	}

	public HashMap<Integer, List<String>> getLcPositionsMap() {
		return lcPositionsMap;
	}

	public void setLcPositionsMap(HashMap<Integer, List<String>> lcPositionsMap) {
		this.lcPositionsMap = lcPositionsMap;
	}

	public boolean isFinish() {
		for(ScheduleTask task:childTasks){
			if(!task.isFinish()){
				return false;
			}
		}
		return true;
	}

	public void setFinish(boolean isFinish) {
		this.isFinish = isFinish;
	}

	public String getGroupDays() {
		return groupDays;
	}

	public void setGroupDays(String groupDays) {
		this.groupDays = groupDays;
	}

	/**
	 * 当前轮次可以安排的位置
	 * @return
	 */
	public void init(ScheduleRule schrule){
		int msp = 0;
		for(ScheduleTask task:childTasks){
			if(task.getSpNum()>msp){
				msp = task.getSpNum();
			}
		}
		this.setGroupMaxSpNum(msp);
		//
		int perlcnum = (int) ((
				(maxDays-1)*(classAmNum+classPmNum)
				+(halfAtLast?classAmNum:(classAmNum+classPmNum)))/
				avgTaskNum);
		HashMap<Integer,String> indexPosMap = new HashMap<Integer, String>();
		for(int i=0;i<maxDays;i++){
			if(i==maxDays-1&&halfAtLast){
				
			}else{
				for(int j=0;j<(classAmNum+classPmNum);j++){
					indexPosMap.put((i*(classAmNum+classPmNum)+j), (i+","+j));
				}
			}
		}
		int lc =0;	
		int index = (int) Math.ceil(avgTaskNum);
		List<Integer> alllc = new ArrayList<Integer>();
		if(avgTaskNum>=maxDays){
			int alltasknum = index;
			int spNum = this.groupMaxSpNum;
			for(int j=0;j<maxDays;j++){
				List<String> poss = new ArrayList<String>();
				for(int k=0;k<(classAmNum+classPmNum);k++){
					poss.add(j+","+k);
				}
				lcPositionsMap.put(alltasknum, poss);
				alllc.add(alltasknum);
				alltasknum--;
				if(spNum>0){
					lcPositionsMap.put(alltasknum, poss);
					alllc.add(alltasknum);
					alltasknum--;
					spNum--;
				}
			}
		}else{
			//计算每天最大排课数
			HashMap<Integer,Integer> dayRuleNum = new HashMap<Integer, Integer>();
			int sumunpos = 0;
			for(int i=0;i<maxDays;i++){
				int unpos = 0;
				for(int j=0;j<classAmNum+classPmNum;j++){
					boolean canar = true;
					for(String teacherId:teacherIds){
						SchRuleTeacher sch = schrule.getRuleTeachers().get(teacherId);
						
						if(sch!=null&&sch.getPositions().size()>0){
							if(sch.getPositions().containsKey(i+","+j)){
								if(sch.getPositions().get(i+","+j).getRuleType()==0){
									canar = false;
									break;
								}
							}
						}
					}
					if(canar){
						SchRuleCourse sch = schrule.getRuleCourses().get(courseId);
						if(sch!=null&&sch.getPositions().size()>0){
							if(sch.getPositions().containsKey(i+","+j)){
								if(sch.getPositions().get(i+","+j).getRuleType()==0){
									canar = false;
								}
							}
						}
					}
					if(!canar){
						unpos++;
						sumunpos++;
					}
				}
				dayRuleNum.put(i, classAmNum+classPmNum-unpos);
			}
			
			int totalDay = maxDays;
			if(halfAtLast){
				totalDay -- ;
			}
			if(index==totalDay){
				//每天一轮
				while(index>0){
					lc++;
					List<String> poss = new ArrayList<String>();
					for(int i=0;i<classAmNum+classPmNum;i++){
						poss.add((index-1)+","+i);
					}
					lcPositionsMap.put(lc, poss);
					
					alllc.add(lc);
					index --;
				}
			}else if(index==totalDay-1){
				//只差距一天的情况 找出最少一天排除掉
				int fi = index ;
				int minnum  =  dayRuleNum.get(0);
				for(int xc=index+1;xc>0;xc--){
					int ar = dayRuleNum.get(xc-1);
					if(ar<minnum){
						minnum= ar;
						//最少的周*
						fi=xc;
					}
				}
				int record = 0;
				index = index+1;
				while(index>0){
					if(index==fi){
						index --;
						record = lc;
						continue;
					}
					lc++;
					List<String> poss = new ArrayList<String>();
					for(int i=0;i<classAmNum+classPmNum;i++){
						poss.add((index-1)+","+i);
					}
					lcPositionsMap.put(lc, poss);
					alllc.add(lc);
					index --;
				}
				//将多余的一天位置放在附近
				List<String> temppos = new ArrayList<String>();
				for(int i=0;i<classAmNum+classPmNum;i++){
					temppos.add(fi-1+","+i);
				}
				//将多余的一天安排在右边
				if(lcPositionsMap.containsKey(record-1)){
					lcPositionsMap.get(record-1).addAll(temppos);
				}else if(lcPositionsMap.containsKey(record)){
					lcPositionsMap.get(record).addAll(temppos);
				}
				
			}else if(index==1){
				List<String> poss = new ArrayList<String>();
				for(int i=0;i<totalDay;i++){
					int day = i;
					for(int j=0;j<classAmNum+classPmNum;j++){
						poss.add(day+","+j);
					}
				}
				alllc.add(1);
				lcPositionsMap.put(1, poss);
			}else if(index==2){
				int sumLessons = (maxDays-1)*(classAmNum+classPmNum)
						+(halfAtLast?0:(classAmNum+classPmNum)) - sumunpos;

				int half = sumLessons/2;
				List<String> poss1 = new ArrayList<String>();
				List<String> poss2 = new ArrayList<String>();
				boolean isLeft  = true;
				int leftNum = 0;
				for(int i=0;i<totalDay;i++){
					int day = i;
					leftNum += dayRuleNum.get(day);
					if(leftNum>=half){
						isLeft = false;
					}
					for(int j=0;j<(classAmNum+classPmNum);j++){
						if(isLeft){
							poss1.add(day+","+j);
						}else{
							poss2.add(day+","+j);
						}
					}
				}
				lcPositionsMap.put(1, poss1);
				lcPositionsMap.put(2, poss2);
				alllc.add(1);
				alllc.add(2);	
			}else{
				//五天三节的情况 或者六天三节、四节
				int fi = index ;
				int minnum  =  dayRuleNum.get(0);
				for(int xc=index;xc>0;xc--){
					int ar = dayRuleNum.get(xc-1);
					
					if(ar<minnum){
						minnum= ar;
						fi=xc;
					}
				}
				if(avgTaskNum==3&&childTasks.size()>5&&maxDays==5){
					List<String> poss = new ArrayList<String>();
					for(int day=0;day<2;day++){
						for(int j=0;j<(classAmNum+classPmNum);j++){
							if(day==1&&j>=classAmNum){
								continue;
							}
							poss.add(day +","+j);
						}
					}
					lcPositionsMap.put(1, poss);
					poss = new ArrayList<String>();
					for(int day=1;day<3;day++){
						for(int j=0;j<(classAmNum+classPmNum);j++){
							if(day==1&&j<classAmNum){
								continue;
							}
							poss.add(day +","+j);
						}
					}
					lcPositionsMap.put(2, poss);
					poss = new ArrayList<String>();
					for(int day=3;day<5;day++){
						for(int j=0;j<(classAmNum+classPmNum);j++){
							poss.add(day +","+j);
						}
					}
					lcPositionsMap.put(3, poss);
				}else{
					List<Integer> days = new ArrayList<Integer>();
					//被排除的天
					List<Integer> exDays = new ArrayList<Integer>();
					int tt  =0;
					while(days.size()<index&&tt<1000){
						tt++;
						int day = (int)(Math.random()* totalDay)+1;
						if(day!=fi&&!days.contains(day)&&notAllLx(days,index,day)){
							days.add(day);
						}
					}
					for(int day :dayRuleNum.keySet()){
						if(!days.contains((day+1))){
							exDays.add(day+1);
						}
					}
					Collections.sort(days);
					//生成位置
					Map<Integer,Integer> lcmap = new HashMap<Integer, Integer>();
					for(int i=0;i<days.size();i++){
						int day = days.get(i);
						List<String> poss = new ArrayList<String>();
						for(int j=0;j<(classAmNum+classPmNum);j++){
							if(day==maxDays&&j>=classAmNum&&halfAtLast){
								continue;
							}
							poss.add((day-1)+","+j);
						}
						int lcc = i+1;
						lcPositionsMap.put(lcc, poss);
						alllc.add(lcc);
						lcmap.put(day, lcc);
					}
					//将多余的放进去
					List<Integer> addedDays = new ArrayList<Integer>();
					for(int day:exDays){
						List<String> temppos = new ArrayList<String>();
						for(int j=0;j<(classAmNum+classPmNum);j++){
							if(day==maxDays&&j>=classAmNum&&halfAtLast){
								continue;
							}
							temppos.add((day-1)+","+j);
						}
						Integer addlc = null;
						for(int addDay:days){
							if(day==addDay+1&&!addedDays.contains(addDay)){
								addedDays.add(addDay);
								addlc = lcmap.get(addDay);
								break;
							}
							if(day==addDay-1&&!addedDays.contains(addDay)){
								addedDays.add(addDay);
								addlc = lcmap.get(addDay);
								break;
							}
						}
						if(addlc!=null){
							lcPositionsMap.get(addlc).addAll(temppos);
						}
//						if(alllc.contains(lcmap.get(day+1))){
//							lcPositionsMap.get(lcmap.get(day+1)).addAll(temppos);
//						}else if(alllc.contains(lcmap.get(day))){
//							lcPositionsMap.get(lcmap.get(day)).addAll(temppos);
//						}
					}
				}
				
			}
			
		}
		
		
		for(ScheduleTask task:childTasks){
			task.setAlllc(alllc);
		}
	}

	/**
	 * 是否连续了
	 * @param days
	 * @param index
	 * @param day
	 * @return
	 */
	private boolean notAllLx(List<Integer> days, int index, int day) {
		// TODO Auto-generated method stub
		if(days.size()==index-1 ){
			days.add(day);
			Collections.sort(days);
			boolean isAlllx = true;
			for(int i=0;i<days.size()-1;i++){
				if(days.get(i)!=(days.get(i+1)-1)){
					isAlllx = false;
					break;
				}
			}
			days.remove((Object) day);
			return !isAlllx;
		}else{
			return true;
		}
	}

	/**
	 * 根据位置获取轮次
	 * @param day
	 * @param lesson
	 * @return
	 */
	public int getLcByPosition(int day,int lesson){
		
		int lc =0 ;
		int maxlc = 0;
		for(Iterator<Integer> it = lcPositionsMap.keySet().iterator();it.hasNext();){
			int lcc = it.next();
			if(lcc>maxlc){
				maxlc = lcc;
			}
			List<String> vals = lcPositionsMap.get(lcc);
			if(vals.contains(day+","+lesson)){
				return lcc;
			}
		}
		if(lc==0){
			lc = maxlc;
		}
		return lc;
	}
}
