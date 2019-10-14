package com.talkweb.schedule.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.schedule.entity.TSchScheduleGradeset;
import com.talkweb.schedule.entity.TSchStudentTclassRelationship;
import com.talkweb.schedule.entity.TSchTask;
import com.talkweb.schedule.entity.TSchTclass;

public interface ScheduleExternalDao {

	List<JSONObject> getScheduleForExam(Map<String, Object> map);

	List<JSONObject> getSubjectForExam(Map<String, Object> map);

	List<JSONObject> getStudentForExam(Map<String, Object> map);

	List<JSONObject> getTclassForExam(Map<String, Object> map);
	
	List<JSONObject> getExamTclassSubMap(Map<String,Object> map);
	
	
	List<TSchTclass> getTclassList(Map<String,Object> map);
	
	
	List<JSONObject> querySubjLevelAndIdInClass(Map<String, Object> map);
	
	List<Long> querySubjIdInTSchTask(Map<String, Object> map);
	
	TSchScheduleGradeset queryPlacementInfoByGrade(Map<String, Object> map);
	
	List<JSONObject> queryTClassInfoList(Map<String, Object> map);
	
	List<TSchTask> queryTSchTask(Map<String, Object> map);
	
	List<TSchStudentTclassRelationship> queryStudentInfoList(Map<String, Object> map);
}
