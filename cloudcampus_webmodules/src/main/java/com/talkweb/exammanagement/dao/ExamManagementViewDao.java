package com.talkweb.exammanagement.dao;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
public interface ExamManagementViewDao {
	
	public List<JSONObject> getExamPlaceInfo(Map<String, Object> param);
	
	public List<JSONObject> getTableCornerList(Map<String, Object> param);
	public int getTableCornerListcount(Map<String, Object> param);
	
	public List<JSONObject> getTClassAndExamPlace(Map<String, Object> param); 
	
	public int getTClassAndExamPlaceCount(Map<String, Object> param);
	
	public List<JSONObject> getStudsAndExamPlace(Map<String, Object> param); 
	
	public int getStudsAndExamPlaceCount(Map<String, Object> param); 
	
	public List<JSONObject> getTableCornerSeatList(Map<String, Object> param);
	public int getTableCornerSeatListCount(Map<String, Object> param);

	public List<JSONObject> getExamSubjectDist(Map<String, Object> param);
	
	
}
