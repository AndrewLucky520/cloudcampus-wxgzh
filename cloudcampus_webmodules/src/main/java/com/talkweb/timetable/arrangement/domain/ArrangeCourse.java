package com.talkweb.timetable.arrangement.domain;

import java.util.HashMap;
import java.util.Map;
/**
 * 课程-班级任务安排
 * @author talkweb
 *
 */
public class ArrangeCourse implements Comparable<ArrangeCourse> {

	/**
	 * 使用年级
	 */
	private String gradeId;
	/**
	 * 用于科目规则 存放年级代码
	 */
	private String gradeLev;
	private String classId;
	private String className;
	
	private String courseId;
	private String courseName;
	/**
	 * 合班组代码
	 */
	private String mcGroupId;
	
	/**
	 * 已安排课程的序列
	 */
	private Map<Double,Integer[]> hasArrangedPos = new HashMap<Double, Integer[]>();
	
	public Map<Double, Integer[]> getHasArrangedPos() {
		return hasArrangedPos;
	}

	public void setHasArrangedPos(Map<Double, Integer[]> hasArrangedPos) {
		this.hasArrangedPos = hasArrangedPos;
	}

	/**
	 * 课程的连排次数（或连排），每次连排2节
	 */
	private int unitSize = 1;
	
	/**
	 * 课程的计划总课时数
	 */
	private double taskLessons = 1;
	
	
	/**
	 * 课程级别  0：主课，1：普课，2：辅课
	 */
	private int courseLevel;
	/**
	 * 固排课数量
	 */
	private int fixedCourseNum;
	
	private boolean merge = false;

	
	private Map<String,ArrangeTeacher> arrangeTeachers = new HashMap<String, ArrangeTeacher>();
	
	
	private ArrangeCourse dwArrangeCourse;
	public ArrangeCourse getDwArrangeCourse() {
		return dwArrangeCourse;
	}

	public void setDwArrangeCourse(ArrangeCourse dwArrangeCourse) {
		this.dwArrangeCourse = dwArrangeCourse;
	}

	/**
	 * 
	 * @param teacherId
	 * @param arrangeTeacher
	 */
	
	public void addTeacher(String teacherId, ArrangeTeacher arrangeTeacher){
		if(!this.arrangeTeachers.containsKey(teacherId)){
			this.arrangeTeachers.put(teacherId, arrangeTeacher);
		}
	}
	
	/**
	 * 课程优先级权重，越小则越优先
	 * @param availablePositions
	 * @return
	 */
	private double weight() {
		double weight = 0;
		//优先级计算函数：f = Tr - Tp
		//Tr是空课表上满足约束条件的、 可供此课程安排的课时数 , Tp是教学计划上该课程的课时数。f越小说明排课的难度越大 ,就应该优先安排		
		//weight = this.availablePositions - this.taskLessons;
		weight = this.taskLessons * -1 + this.courseLevel * 50;
		
		if(this.needOdd()){
			weight = -9990;
		}
		
		if(this.merge) {
			weight = -9999;
		}
		
		return weight;
	}
	
	public boolean needOdd(){
		return taskLessons - Math.floor(taskLessons) == 0.5;
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

	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	public int getUnitSize() {
		return unitSize;
	}
	public void setUnitSize(int unitSize) {
		this.unitSize = unitSize;
	}


	public Map<String, ArrangeTeacher> getArrangeTeachers() {
		return arrangeTeachers;
	}

	public void setArrangeTeachers(Map<String, ArrangeTeacher> arrangeTeachers) {
		this.arrangeTeachers = arrangeTeachers;
	}

	public double getTaskLessons() {
		return taskLessons;
	}

	public void setTaskLessons(double taskLessons) {
		this.taskLessons = taskLessons;
		
	}

	public int getCourseLevel() {
		return courseLevel;
	}

	public void setCourseLevel(int courseLevel) {
		this.courseLevel = courseLevel;
	}

	@Override
	public int compareTo(ArrangeCourse o) {
		//小于-1、等于0, 大于1
		return this.weight() < o.weight() ? -1 : this.weight() == o.weight() ? 0 : 1;
	}



	public int getFixedCourseNum() {
		return fixedCourseNum;
	}

	public void setFixedCourseNum(int fixedCourseNum) {
		this.fixedCourseNum = fixedCourseNum;
	}

	public boolean isMerge() {
		return merge;
	}

	public void setMerge(boolean merge) {
		this.merge = merge;
	}
	
	/**
	 * 判断是否需要控制教学进度
	 * @return
	 */

	public boolean getNeedCtrlProc(){
		return this.taskLessons>1.0?true:false;
	}

	public String getGradeId() {
		return gradeId;
	}

	public void setGradeId(String gradeId) {
		this.gradeId = gradeId;
	}

	public String getGradeLev() {
		return gradeLev;
	}

	public void setGradeLev(String gradeLev) {
		this.gradeLev = gradeLev;
	}

	public String getMcGroupId() {
		return mcGroupId;
	}

	public void setMcGroupId(String mcGroupId) {
		this.mcGroupId = mcGroupId;
	}
	
	
}
