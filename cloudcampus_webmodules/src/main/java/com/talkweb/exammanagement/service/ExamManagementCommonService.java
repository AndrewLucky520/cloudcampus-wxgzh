package com.talkweb.exammanagement.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface ExamManagementCommonService {
	JSONObject getStatus(JSONObject request);
	
	List<JSONObject> getGradeList(JSONObject request);
	
	List<JSONObject> getScheduleList(JSONObject request);
	
	List<JSONObject> getSubjectList(JSONObject request);
	
	List<JSONObject> getExamPlanList(JSONObject request);
	
	List<JSONObject> getExamSubjectList(JSONObject request);
	
	List<JSONObject> getGroupSubjectList(JSONObject request);
	
	List<JSONObject> getTClassList(JSONObject request);
	
	List<JSONObject> getExamManagementList(JSONObject request);
	
	List<JSONObject> getScoreList(JSONObject request);

	List<JSONObject> getTClassListByGroupId(JSONObject request);

	List<JSONObject> getPlaceInfoList(JSONObject request);
}
