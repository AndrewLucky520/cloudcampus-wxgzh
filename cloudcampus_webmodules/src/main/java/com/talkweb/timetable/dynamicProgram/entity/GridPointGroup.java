package com.talkweb.timetable.dynamicProgram.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GridPointGroup implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6688636420545327638L;

	private String gradeId;
	private String gradeLevel;
	
	private int day;
	private int lesson;
	
	private List<GridPoint> pointList = new ArrayList<GridPoint>();
	
	private HashMap<String,List<GridPoint>> classPointMap = new  HashMap<String, List<GridPoint>>();

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

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getLesson() {
		return lesson;
	}

	public void setLesson(int lesson) {
		this.lesson = lesson;
	}

	public List<GridPoint> getPointList() {
		return pointList;
	}

	public void setPointList(List<GridPoint> pointList) {
		this.pointList = pointList;
	}

	public HashMap<String, List<GridPoint>> getClassPointMap() {
		return classPointMap;
	}

	public void setClassPointMap(HashMap<String, List<GridPoint>> classPointMap) {
		this.classPointMap = classPointMap;
	}
	/**
	 * 课眼加课
	 * @param gridPoint
	 */
	public void addClassPointMap(GridPoint gridPoint) {
		List<GridPoint> gList = new ArrayList<GridPoint>();
		if(classPointMap.containsKey(gridPoint.getClassId())){
			gList = classPointMap.get(gridPoint.getClassId());
			gList.add(gridPoint);
		}else{
			gList.add(gridPoint);
		}
		
		pointList.add(gridPoint);
	}
	/**
	 * 课眼移除课
	 * @param gridPoint
	 */
	public void removeClassPointMap(GridPoint gridPoint) {
		if(classPointMap.containsKey(gridPoint.getClassId())){
			List<GridPoint> gList = classPointMap.get(gridPoint.getClassId());
			gList.remove(gridPoint);
		}
		
		pointList.remove(gridPoint);
	}
}
