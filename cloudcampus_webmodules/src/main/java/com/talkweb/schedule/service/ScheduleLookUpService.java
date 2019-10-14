package com.talkweb.schedule.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface ScheduleLookUpService {
	
	int addSchedule(Map<String, Object> map);

	int updateSchedule(JSONObject object);

	void deleteSchedule(JSONObject object);
	
	List<JSONObject> getSchedule(Map<String, String> map);
	
	public List<JSONObject> getClassTb(Map<String,Object> param);
	public List<JSONObject> getClassTbTeachers(Map<String,Object> param);
	public List<JSONObject> getClassTbZOU(Map<String,Object> param);
	public List<JSONObject> getTeacherTb(Map<String,Object> param);
	public List<JSONObject> getStudentTb(Map<String,Object> param);
	public List<JSONObject> getGroundTb(Map<String,Object> param);
	public List<JSONObject> getGradeTbs(Map<String,Object> param);
	public List<JSONObject> getStudentClassTbs(Map<String,Object> param);
	
	public JSONObject getNewTimetablePrintSet(Map<String,Object> param);
	
	public void updateNewTimetablePrintSet(Map<String,Object> param);
	
	public JSONObject getScheduleTimetableById(Map<String, Object> param);
	
	public void insertNewTimetablePrintSet(Map<String,Object> param);
	/**
	 * schoolId,tclassIdList,schoolYear,termInfoId(1,2),subjectId(options)
	 * @param object
	 * @return
	 */
	public List<JSONObject> getStuScheduleList(JSONObject object);

	List<JSONObject> getClassTbZOU1(HashMap<String, Object> param);
	
	//新高考成绩模块
	List<JSONObject> getStuInTclass(Map<String, Object> param);
	
	//新高考教学考评模块
	List<JSONObject> evalOfSubInSchedule(Map<String, Object> param);

	List<JSONObject> getStudentTbForApp(Map<String, Object> param);
}
