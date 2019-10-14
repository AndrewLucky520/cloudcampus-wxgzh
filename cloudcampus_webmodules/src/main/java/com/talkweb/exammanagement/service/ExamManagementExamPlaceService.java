package com.talkweb.exammanagement.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface ExamManagementExamPlaceService {
	public List<JSONObject> getExamPlaceList(Map<String, Object> param);
	
	
	public JSONObject getExamPlaceListMap(HashMap param);
	
	public List<JSONObject> getHasOldExamPlaceList(Map<String, Object> param);
	
	public void deleteExamPlace(Map<String, Object> param);
	
	
	public List<JSONObject> getExamPlace(Map<String, Object> param);
	
	public void saveExamPlace(Map<String, Object> param);
	
	public void saveCopyExamPlace(Map<String, Object> param);


	int hasOldExamPlaceList(Map<String, Object> param);

}
