package com.talkweb.exammanagement.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface ExamManagementSetDao {
		
	
	public List<JSONObject> getExamPlaceList(Map<String, Object> param);
	
	public List<JSONObject> getHasOldExamPlaceList(Map<String, Object> param);
	
	public void deleteExamPlace(Map<String, Object> param);
	
	public List<JSONObject> getExamPlace(Map<String, Object> param);
	
	public void saveExamPlace(Map<String, Object> param);
	
	public List<JSONObject> getOldExamPlace(Map<String, Object> param);
	
	public List<JSONObject> getNonparticipationExamList(Map<String, Object> param);
	public void saveNonparticipationExamList(Map<String, Object> param);
	public void deleteNonparticipationExamList(Map<String, Object> param);
	public List<JSONObject> getUserExamPlace(Map<String, Object> param);
	
	public List<JSONObject> studsWaiting(Map<String, Object> param);
	public List<JSONObject> getStudsInExamPlace(Map<String, Object> param);
	
	public void saveArrangeExamResult(Map<String, Object> param);
	public void deleteArrangeExamResult(Map<String, Object> param);
	
	public void saveStudsWaiting(Map<String, Object> param);
	public void deleteStudsWaiting(Map<String, Object> param);

	void updateExamPlace(Map<String, Object> param);

	List<JSONObject> getExplantSubject(Map<String, Object> param);

	public void deleteExamResult(Map<String, Object> param);

	public List<JSONObject> getMaxtestNumber(Map<String, Object> param);

	public List<JSONObject> getSubjectGbyResult(Map<String, Object> param);

	public List<JSONObject> getstudsWaitingSubject(JSONObject request);
	
}
