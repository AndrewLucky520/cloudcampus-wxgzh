package com.talkweb.timetable.arrangement.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.talkweb.timetable.arrangement.algorithm.CourseGene;

/**
 * 排课表格数据模型
 * 
 * @author Li xi yuan
 *
 */
public class ArrangeGrid {
	
	private String schoolId;
	
	private String timetableId;
		
	private List<String> errorInfos = new ArrayList<String>();
	
	private Map<String,ArrangeClass> classTimetables = new HashMap<String, ArrangeClass>();
	
	public boolean hasClassTimetable(String classId) {
		return this.classTimetables.containsKey(classId);
	}
	/**
	 * 重置所有班级的课表
	 */
	public void resetAllTimetable(){
		for (ArrangeClass arrangeClass : classTimetables.values()) {
			arrangeClass.initTimetable();
		}
	}
	
	public ArrangeClass getArrangeClass(String classId) {
		return this.classTimetables.get(classId);
	}
	
	public CourseGene[][] getClassTimetable(String classId) {
		return this.getArrangeClass(classId).getTimetable();
	}
	
	public boolean addArrangeClass(ArrangeClass arrangeClass) {
		return this.classTimetables.put(arrangeClass.getClassId(), arrangeClass) != null ? true : false;
	}

	public Map<String, ArrangeClass> getClassTimetables() {
		return classTimetables;
	}

	public void setClassTimetables(Map<String, ArrangeClass> classTimetables) {
		this.classTimetables = classTimetables;
	}

	public String getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}

	public String getTimetableId() {
		return timetableId;
	}

	public void setTimetableId(String timetableId) {
		this.timetableId = timetableId;
	}

	public List<String> getErrorInfos() {
		return errorInfos;
	}

	public void setErrorInfos(List<String> errorInfos) {
		this.errorInfos = errorInfos;
	}
	

	public void addErrorInfo(String errorInfo) {
		this.errorInfos.add(errorInfo);
	}
	
	
	
}
