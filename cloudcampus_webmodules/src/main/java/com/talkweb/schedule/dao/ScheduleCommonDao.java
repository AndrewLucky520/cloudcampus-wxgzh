package com.talkweb.schedule.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface ScheduleCommonDao {
	
	JSONObject getScheduleGradePlacementInfo(HashMap<String, Object> map);

	List<JSONObject> getScheduleGradeList(HashMap<String, Object> map);
	
	List<JSONObject> getScheduleGradeListWithTeaching(HashMap<String, Object> map);

	List<JSONObject> getScheduleSubjectList(HashMap<String, Object> map);

	List<JSONObject> getScheduleSubjectWithTeaching(HashMap<String, Object> map);

	List<JSONObject> getScheduleTeacherList(HashMap<String, Object> map);
	
	List<JSONObject> getTeachingTaskGradeList(HashMap<String, Object> map);
	
	List<JSONObject> getScheduleSubjectGroupList(HashMap<String, Object> map);
	
	List<JSONObject> getScheduleSubjectGroupTclassList(HashMap<String, Object> map);
	
	List<JSONObject> getScheduleSubjectTclassList(JSONObject object);

	List<JSONObject> getScheduleGroundList(JSONObject object);
	
	List<JSONObject> getScheduleAllGroundList(JSONObject object);
	
	public JSONObject getSchedule(Map<String, Object> param);

	String selectPlacementIdInSchedule(String placementId);
	
	List<JSONObject> getSchedualListByTeacher(Map<String, Object> params);

	JSONObject getLatestPublishedScheduleByStudent(JSONObject param);
}