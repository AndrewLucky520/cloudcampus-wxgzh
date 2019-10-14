package com.talkweb.exammanagement.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.exammanagement.domain.ExamPlan;

public interface ExamManagementViewService {
	
	public List<JSONObject> getExamPlaceInfo(Map<String, Object> param);
	
	public JSONObject getTableCornerList(Map<String, Object> param);
	public int getTableCornerListCount(Map<String, Object> param);
	
	
	public JSONObject getTClassAndExamPlace(Map<String, Object> param); 
	
	public int getTClassAndExamPlaceCount(Map<String, Object> param); 
	
	public JSONObject getStudsAndExamPlace(Map<String, Object> param); 
	
	public int getStudsAndExamPlaceCount(Map<String, Object> param);

	List<ExamPlan> getExamPlanList(Map<String, Object> param); 
	
	
}	
