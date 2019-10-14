package com.talkweb.timetable.dynamicProgram.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.talkweb.timetable.dynamicProgram.enums.CourseLevel;

public class ScheduleTask implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -463632781447045867L;

	private String taskGroupId;
	private String taskId;
	private String gradeId;
	private String gradeLevel;
	private String classId;
	private String courseId;
	private List<String> teacherIds;
	
	private double taskNum;
	private double arrangedTaskNum = 0;
	//优先安排轮次
	private List<Integer> firstArLcs = new ArrayList<Integer>();
	//优先安排位置
	private List<String> firstArPoss = new ArrayList<String>();
	
	/**
	 * 课程数是否小于于天数
	 */
	private boolean isNeedCourseNumControl ;
	/**
	 * 课程上午、下午
	 */
	private CourseLevel courseLevel;
	
	
	private CourseLevel orCourseLevel;
	
	private List<Integer> preSingleLcs= null;
	
	/**
	 * 上午课 是否完全排上午 默认为是
	 */
	private boolean isAllAmFirst = true;
	
	public CourseLevel getOrCourseLevel() {
		return orCourseLevel;
	}
	public void setOrCourseLevel(CourseLevel orCourseLevel) {
		this.orCourseLevel = orCourseLevel;
	}
	/**
	 * 上午课所用参数 控制上午最小排课数 且与年级有关
	 */
	private double amMinPercent;
	
	/**
	 * 上午课所用参数 控制上午最大排课数 且与年级有关
	 */
	private double amMaxPercent;
	/**
	 * 控制下午最大排课节次
	 */
	private double pmMaxNum;
	
	private int maxDays;
	
	private boolean halfAtLast = false;
	/**
	 * 连排数
	 */
	private int spNum;
	
	private int hasSpNum;
	private int classAmNum;
	
	private int classPmNum;
	/**
	 * 本次排课真实排课程 用于计算排课完成比例
	 */
	private double trueTaskNum = 0;
	

	private boolean isMergeClassCourse = false;
	public boolean isMergeClassCourse() {
		return isMergeClassCourse;
	}
	public void setMergeClassCourse(boolean isMergeClassCourse) {
		this.isMergeClassCourse = isMergeClassCourse;
	}
	/**
	 * 科目排序
	 */
	private int courseOrder;
	
	/**
	 * 所有轮次
	 */
	private List<Integer> alllc = new ArrayList<Integer>();
	
	/**
	 * 未完成的轮次
	 */
	private List<Integer> unfinishLc = new ArrayList<Integer>();
	
	public String getTaskGroupId() {
		return taskGroupId;
	}
	public void setTaskGroupId(String taskGroupId) {
		this.taskGroupId = taskGroupId;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
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
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public List<String> getTeacherIds() {
		return teacherIds;
	}
	public void setTeacherIds(List<String> teacherIds) {
		this.teacherIds = teacherIds;
	}
	public double getTaskNum() {
		return taskNum;
	}
	public void setTaskNum(double taskNum) {
		this.taskNum = taskNum;
	}
	public double getArrangedTaskNum() {
		return arrangedTaskNum;
	}
	public void setArrangedTaskNum(double arrangedTaskNum) {
		this.arrangedTaskNum = arrangedTaskNum;
	}
	public double getUnTaskNum() {
		return taskNum-arrangedTaskNum;
	}
	/**
	 * 是否需要判断课程单日单班数量不大于1规则--返回true则需要 
	 * @return
	 */
	public boolean isNeedCourseNumControl() {
		return isNeedCourseNumControl;
	}
	public void setNeedCourseNumControl(boolean isNeedCourseNumControl) {
		this.isNeedCourseNumControl = isNeedCourseNumControl;
	}
	public CourseLevel getCourseLevel() {
		return courseLevel;
	}
	public void setCourseLevel(CourseLevel courseLevel) {
		this.courseLevel = courseLevel;
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
	public int getSpNum() {
		return spNum;
	}
	public void setSpNum(int spNum) {
		this.spNum = spNum;
	}
	public int getClassAmNum() {
		return classAmNum;
	}
	public void setClassAmNum(int classAmNum) {
		this.classAmNum = classAmNum;
	}
	public int getClassPmNum() {
		return classPmNum;
	}
	public void setClassPmNum(int classPmNum) {
		this.classPmNum = classPmNum;
	}
	public int getCourseOrder() {
		return courseOrder;
	}
	public void setCourseOrder(int courseOrder) {
		this.courseOrder = courseOrder;
	}
	
	public boolean isFinish(){
		return (arrangedTaskNum - taskNum >=0);
//		return taskNum==arrangedTaskNum;
	}
	public List<Integer> getAlllc() {
		return alllc;
	}
	public void setAlllc(List<Integer> alllc) {
		this.alllc = alllc;
	}
	public List<Integer> getUnfinishLc() {
		return unfinishLc;
	}
	public void setUnfinishLc(List<Integer> unfinishLc) {
		this.unfinishLc = unfinishLc;
	}
	public int getNeedSpNum() {
		// TODO Auto-generated method stub
		return spNum-hasSpNum;
	}
	public int getHasSpNum() {
		return hasSpNum;
	}
	public void setHasSpNum(int hasSpNum) {
		this.hasSpNum = hasSpNum;
	}
	
	public void resetUnfinishLc(ScheduleTable scheduleTable) {
		// TODO Auto-generated method stub
		List<Integer> unfinish = new ArrayList<Integer>();
		if(taskGroupId!=null){
			
			ScheduleTaskGroup tg = scheduleTable.getScheduleTaskGroupById(taskGroupId);
			
			GridPointGroup[][] classtb = scheduleTable.getScheduleClassByClassId(classId).getGridPointArr();
			if(tg==null||tg.getLcPositionsMap()==null){
				return;
			}
			Set<Integer> tasklc = tg.getLcPositionsMap().keySet();
			for(Integer lc:tasklc){
				List<String> poss = tg.getLcPositionsMap().get(lc);
//				int day = Integer.parseInt(poss.get(0).split(",")[0]);
				
				if(hasClassInPosList(classtb,poss)){
					continue;
				}else{
					unfinish.add(lc);
				}
			}
			setUnfinishLc(unfinish);
		}else{
//			// 单班不连排 且课程数大于天数的
//			if(spNum==0&&taskNum>=maxDays){
//				GridPointGroup[][] classtb = scheduleTable.getScheduleClassByClassId(classId).getGridPointArr();
//				for(int i=1;i<maxDays+1;i++){
//					if(hasClassOnDay(classtb,i-1)){
//						continue;
//					}else{
//						unfinish.add(i);
//					}
//				}
//				
//				for(int i=maxDays+1;i<=taskNum;i++){
//					
//				}
//					
//			}
		}
	}
	public boolean hasClassOnDay(GridPointGroup[][] classtb, int day) {
		// TODO Auto-generated method stub
		for(int j=0;j<classtb[day].length;j++){
			GridPointGroup gp = classtb[day][j];
			if(gp!=null){
				List<GridPoint> list = gp.getPointList();
				for(GridPoint gpoint:list){
					if(gpoint.getClassId().equalsIgnoreCase(classId)&&gpoint.getCourseId().equalsIgnoreCase(courseId)){
						return true;
					}
				}
			}
		}
		return false;
	}
	public boolean hasClassInPosList(GridPointGroup[][] classtb, List<String> poss) {
		// TODO Auto-generated method stub
		for(String pos :poss){
			int day = Integer.parseInt(pos.split(",")[0]);
			int lesson = Integer.parseInt(pos.split(",")[1]);
			if(classtb[day]!=null&&classtb[day][lesson]!=null){
				GridPointGroup gp = classtb[day][lesson];
				if(gp!=null){
					List<GridPoint> list = gp.getPointList();
					for(GridPoint gpoint:list){
						if(gpoint.getClassId().equalsIgnoreCase(classId)&&gpoint.getCourseId().equalsIgnoreCase(courseId)){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	public double getAmMinPercent() {
		return amMinPercent;
	}
	public void setAmMinPercent(double amMinPercent) {
		this.amMinPercent = amMinPercent;
	}
	public double getAmMaxPercent() {
		return amMaxPercent;
	}
	public void setAmMaxPercent(double amMaxPercent) {
		this.amMaxPercent = amMaxPercent;
	}
	public List<Integer> getFirstArLcs() {
		return firstArLcs;
	}
	public void setFirstArLcs(List<Integer> firstArLcs) {
		this.firstArLcs = firstArLcs;
	}
	public void addFirstArLcs(int firstArLc) {
		if(!firstArLcs.contains((Object) firstArLc)){
			firstArLcs.add((Integer) firstArLc);
		}
	}
	public List<String> getFirstArPoss() {
		return firstArPoss;
	}
	public void setFirstArPoss(List<String> firstArPoss) {
		this.firstArPoss = firstArPoss;
	}
	public void addFirstArPoss( String  firstArPos ) {
		if(!firstArPoss.contains(firstArPos)){
			firstArPoss.add(firstArPos);
		}
	}
	public void rebuildTrueTaskNum() {
		// TODO Auto-generated method stub
		if(taskNum>arrangedTaskNum){
			setTrueTaskNum( taskNum-arrangedTaskNum);
		}else{
			//-1为不需要排的课程
			setTrueTaskNum(-1);
		}
	}
	public double getTrueTaskNum() {
		if(trueTaskNum==-1){
			return 0;
		}else if(trueTaskNum==0){
			return taskNum;
		}else{
			return trueTaskNum;
		}
	}
	public void setTrueTaskNum(double trueTaskNum) {
		this.trueTaskNum = trueTaskNum;
	}
	public boolean isAllAmFirst() {
		return isAllAmFirst;
	}
	public void setAllAmFirst(boolean isAllAmFirst) {
		this.isAllAmFirst = isAllAmFirst;
	}
	public double getPmMaxNum() {
		return pmMaxNum;
	}
	public void setPmMaxNum(double pmMaxNum) {
		this.pmMaxNum = pmMaxNum;
	}
	public List<Integer> getPreSingleLcs() {
		return preSingleLcs;
	}
	public void setPreSingleLcs(List<Integer> preSingleLcs) {
		this.preSingleLcs = preSingleLcs;
	}
}
