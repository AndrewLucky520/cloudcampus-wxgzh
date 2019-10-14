package com.talkweb.exammanagement.service;

import java.text.ParseException;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface ExamManagementExamPlanService {
	List<JSONObject> getExamPlanList(JSONObject request);
	
	void deleteExamPlan(JSONObject request);
	
	JSONObject getExamPlan(JSONObject request);
	
	void saveExamPlan(JSONObject request) throws ParseException;
}
