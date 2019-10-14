package com.talkweb.exammanagement.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface ExamManagementExamStudsService {
	
	
	public JSONObject getNonparticipationExamList(Map<String, Object> param);
	public void saveNonparticipationExamList(Map<String, Object> param);
	
	public JSONObject getUserExamPlace(Map<String, Object> param);
	
	public List<JSONObject> studsWaiting(Map<String, Object> param);
	public JSONObject getStudsInExamPlace(Map<String, Object> param);
	
	public void deleteStudToExamPlace(Map<String, Object> param);
	
	
	public void saveArrangeExamResult(Map<String, Object> param);
	List<JSONObject> getStudsInExamPlaceByAccountId(Map<String, Object> param);
	void sortExamResult(Map<String, Object> param);
	void deleteStudToWait(Map<String, Object> param);
	void serializabdeleteNonparticipation(Map<String, Object> param);
}
