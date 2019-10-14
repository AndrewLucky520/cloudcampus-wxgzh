package com.talkweb.timetable.dynamicProgram.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.talkweb.timetable.dynamicProgram.rule.ScheduleRule;

public class ScheduleClass  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -506959077918862008L;

	private String synj;
	private String gradeLevel;
	private String classId;
	private String className;
	/**
	 * 班级任务数
	 */
	private double classTaskNum;
	/**
	 * 已安排课程数
	 */
	private double hasArrangedNum = 0;
	private int maxDays;
	private int amNum;
	private int pmNum;
	/**
	 * 最后一天是否上半天
	 */
	private boolean halfAtLast;
	/**
	 * 最大可排课数
	 */
	private int maxPosNum;
	
	private List<ScheduleTask> classTasks = new ArrayList<ScheduleTask>();
	
	private List<GridPoint> finishLessons = new ArrayList<GridPoint>();
	
	private HashMap<Integer,HashMap<Integer,List<GridPoint>>> finishLessonMap = 
			new HashMap<Integer, HashMap<Integer,List<GridPoint>>>();
	
	private GridPointGroup[][] gridPointArr ;
	
	private HashMap<Integer,Integer> daySpNumMap = new HashMap<Integer, Integer>();
	public HashMap<Integer, Integer> getDaySpNumMap() {
		return daySpNumMap;
	}

	public void setDaySpNumMap(HashMap<Integer, Integer> daySpNumMap) {
		this.daySpNumMap = daySpNumMap;
	}

	private int maxSpOnDay = 0;
		
	public void init(ScheduleRule schRule,List<String> commonposs,ScheduleTable table){
		this.gridPointArr = new GridPointGroup[maxDays][amNum+pmNum];
		this.maxPosNum =  (maxDays-1)*(amNum+pmNum) + (halfAtLast?amNum:(amNum+pmNum)) ;
		int totalSp = 0;
		for(ScheduleTask task:classTasks){
			totalSp+= task.getSpNum();
		}
//		if(totalSp<10){
			this.maxSpOnDay = (int) Math.ceil((float)totalSp/maxDays) ;
//		}else{
//			this.maxSpOnDay = (int) Math.ceil((float)totalSp/maxDays)+1;
//		}
		HashMap<ScheduleTask,HashMap<Integer,Integer>> taskOnDayCantNum = new HashMap<ScheduleTask, HashMap<Integer,Integer>>();
		for(int i=0;i<maxDays;i++){
			for(int j=0;j<(amNum+pmNum);j++){
				if(halfAtLast&&j>(amNum-1)){
					continue;
				}
				String pos = i+","+j;
				if(commonposs.contains(pos)){
					//设置教学任务的优先排课位置和轮次
					int uncount = 0;
					List<ScheduleTask> canTask = new ArrayList<ScheduleTask>();
					for(ScheduleTask task:classTasks){
						if(task.getTaskNum()<maxDays){
							continue;
						}
						if(!schRule.canArrangePosition(task, i, j, 0, table, 1)){
							uncount++;
							HashMap<Integer,Integer> daymap = new HashMap<Integer, Integer>();
							if(taskOnDayCantNum.containsKey(task)){
								daymap =   taskOnDayCantNum.get(task) ;
							}
							if(daymap.containsKey(i)){
								daymap.put(i, daymap.get(i)+1);
							}else{
								daymap.put(i, 1);
							}
							taskOnDayCantNum.put(task, daymap);
						}else{
							canTask.add(task);
						}
					}
					if(uncount>0){
						for(ScheduleTask task:canTask){
							if(task.getTaskGroupId()!=null){
								ScheduleTaskGroup sg = table.getAllTaskGroupMap().get(task.getTaskGroupId());
								if(sg!=null){
									int lc = sg.getLcByPosition(i, j);
									task.addFirstArLcs(lc);
								}
							}else{
								task.addFirstArLcs((i));
							}
							task.addFirstArPoss(pos);
						}
					}
					
				}
			}
		}
		//设置优先排天
		for(Iterator<ScheduleTask> it = taskOnDayCantNum.keySet().iterator();it.hasNext();){
			ScheduleTask task = it.next();
			List<Integer> yxlc = new ArrayList<Integer>();
			HashMap<Integer, Integer> daymap = taskOnDayCantNum.get(task);
			for(Iterator<Integer> daykey = daymap.keySet().iterator();daykey.hasNext();){
				Integer day = daykey.next();
				int num = daymap.get(day);
				if(num>=3){
					if(task.getTaskGroupId()!=null){
						ScheduleTaskGroup sg = table.getAllTaskGroupMap().get(task.getTaskGroupId());
						if(sg!=null){
							int lc = sg.getLcByPosition(day, 0);
							yxlc.add(lc);
						}
					}else{
						yxlc.add(day);
					}
				}
			}
			task.setPreSingleLcs(yxlc);
		}
		
		taskOnDayCantNum = null;
	}
	
	public ScheduleClass(String synj, String gradeLevel, String classId,String className,
			int amNum,int pmNum,boolean halfAtLast,int maxDays ){
		this.synj = synj;
		this.gradeLevel = gradeLevel;
		this.classId = classId;
		this.className = className;
		this.amNum = amNum;
		this.pmNum = pmNum;
		this.halfAtLast = halfAtLast;
		this.maxDays = maxDays;
//		this.init(schRule);
	}

	public String getSynj() {
		return synj;
	}

	public void setSynj(String synj) {
		this.synj = synj;
	}

	public String getGradeLevel() {
		return gradeLevel;
	}

	public void setGradeLevel(String gradeLevel) {
		this.gradeLevel = gradeLevel;
	}

	public String getClassId() {
		return classId;
	}

	public void setClassId(String classId) {
		this.classId = classId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public double getClassTaskNum() {
		double taskSum = 0;
		for(ScheduleTask task: classTasks){
			taskSum+= task.getTrueTaskNum();
		}
		return taskSum;
	}

	public void setClassTaskNum(double classTaskNum) {
		this.classTaskNum = classTaskNum;
	}

	public double getHasArrangedNum() {
		return hasArrangedNum;
	}

	public void setHasArrangedNum(double hasArrangedNum) {
		this.hasArrangedNum = hasArrangedNum;
	}

	public int getMaxDays() {
		return maxDays;
	}

	public void setMaxDays(int maxDays) {
		this.maxDays = maxDays;
	}

	public int getAmNum() {
		return amNum;
	}

	public void setAmNum(int amNum) {
		this.amNum = amNum;
	}

	public int getPmNum() {
		return pmNum;
	}

	public void setPmNum(int pmNum) {
		this.pmNum = pmNum;
	}

	public boolean isHalfAtLast() {
		return halfAtLast;
	}

	public void setHalfAtLast(boolean halfAtLast) {
		this.halfAtLast = halfAtLast;
	}

	public int getMaxPosNum() {
		return maxPosNum;
	}

	public void setMaxPosNum(int maxPosNum) {
		this.maxPosNum = maxPosNum;
	}

	public List<ScheduleTask> getClassTasks() {
		return classTasks;
	}

	public void setClassTasks(List<ScheduleTask> classTasks) {
		this.classTasks = classTasks;
	}

	public void addInitTask(ScheduleTask task) {
		// TODO Auto-generated method stub
		if(!classTasks.contains(task)){
			this.classTasks.add(task);
		}
	}
	public List<GridPoint> getFinishLessons() {
		return finishLessons;
	}

	public void setFinishLessons(List<GridPoint> finishLessons) {
		this.finishLessons = finishLessons;
	}

	public HashMap<Integer, HashMap<Integer, List<GridPoint>>> getFinishLessonMap() {
		return finishLessonMap;
	}

	public void setFinishLessonMap(
			HashMap<Integer, HashMap<Integer, List<GridPoint>>> finishLessonMap) {
		this.finishLessonMap = finishLessonMap;
	}

	public int getMaxSpOnDay() {
		return maxSpOnDay;
	}

	public void setMaxSpOnDay(int maxSpOnDay) {
		this.maxSpOnDay = maxSpOnDay;
	}

	public GridPointGroup[][] getGridPointArr() {
		return gridPointArr;
	}

	public void setGridPointArr(GridPointGroup[][] gridPointArr) {
		this.gridPointArr = gridPointArr;
	}

	/**
	 * 班级课表--加课
	 * @param gridPoint
	 */
	public void addGridPoint(GridPoint gridPoint) {
		// TODO Auto-generated method stub
		int day = gridPoint.getDay();
		int lesson = gridPoint.getLesson();
		double arnum = gridPoint.getArrangedNum();
		hasArrangedNum += arnum;
		finishLessons.add(gridPoint);
		HashMap<Integer, List<GridPoint>> daymap = finishLessonMap.get(day);
		if(daymap==null){
			daymap = new HashMap<Integer, List<GridPoint>>();
		}
		List<GridPoint> glist = daymap.get(lesson);
		if(glist==null){
			glist = new ArrayList<GridPoint>();
			daymap.put(lesson, glist);
		}
		glist.add(gridPoint);
		
		GridPointGroup gp = gridPointArr[day][lesson] ;
		if(gp==null){
			gp = new GridPointGroup();
			gp.setDay(day);
			gp.setGradeId(synj);
			gp.setGradeLevel(gradeLevel);
			gp.setLesson(lesson);
		}
		gp.addClassPointMap(gridPoint);
		gridPointArr[day][lesson] = gp;
	}
	
	public void addSpNumOnDay(int day){
		if(daySpNumMap.containsKey(day)){
			daySpNumMap.put(day, daySpNumMap.get(day)+1);
		}else{
			daySpNumMap.put(day, 1);
		}
	}
	public void removeSpNumOnDay(int day){
		if(daySpNumMap.containsKey(day)){
			if(daySpNumMap.get(day)-1>=0){
				daySpNumMap.put(day, daySpNumMap.get(day)-1);
			}else{
				daySpNumMap.remove(day);
			}
		}
	}
	/**
	 * 班级课表--移除课
	 * @param gridPoint
	 */
	public void removeGridPoint(GridPoint gridPoint) {
		// TODO Auto-generated method stub
		int day = gridPoint.getDay();
		int lesson = gridPoint.getLesson();
		double arnum = gridPoint.getArrangedNum();
		hasArrangedNum -= arnum;
		finishLessons.remove(gridPoint);
		HashMap<Integer, List<GridPoint>> daymap = finishLessonMap.get(day);
		if(daymap!=null){
			List<GridPoint> glist = daymap.get(lesson);
			if(glist!=null){
				glist.remove(gridPoint);
			}
		}
		
		GridPointGroup gp = gridPointArr[day][lesson] ;
		if(gp!=null){
			gp.removeClassPointMap(gridPoint);
		}
		if(gp!=null&&gp.getPointList()!=null&&gp.getPointList().size()==0){
			gridPointArr[day][lesson] = null;
		}
		if(gp==null||gp.getPointList()==null){
			gridPointArr[day][lesson] = null;
		}
	}

	/**
	 * 是否可以安排连排课
	 * @param day
	 * @return
	 */
	public boolean canArrangeSpOnDay(int day) {
		// TODO Auto-generated method stub
		if(daySpNumMap.containsKey(day)){
			int num = daySpNumMap.get(day);
			if(num+1>maxSpOnDay){
				return false;
			}
		}
		return true;
	}
	
	public boolean hasCourseSpOnDay(String courseId,int day){
		
		GridPointGroup[] garr = gridPointArr[day];
		
		if(garr!=null&&garr.length>0){
			
			for(int i=0;i<garr.length;i++){
				GridPointGroup group = garr[i];
				if(group!=null&&group.getPointList()!=null){
					for(GridPoint p:group.getPointList()){
						if(p.getCourseId().equals(courseId)&&p.isSpPoint()){
							return true;
						}
					}
				}
			}
		}
		return false;
		
	}

	
}
