package com.talkweb.scoreManage.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.scoreManage.po.gm.DegreeInfo;

public interface AppScoreReportService {
	JSONObject getStudentInfo(String termInfoId, Long schoolId, Long userId, int examType);
	
	JSONObject getTeacherInfo(String termInfoId, Long schoolId, Long userId, int examType);

	List<JSONObject> getExamList(JSONObject params);

	JSONObject getSchoolExamStudentScoreReport(JSONObject params);
	
	void insertExamViewByExamType(int examType, JSONObject params);
	
	JSONObject getClassExamStudentScoreReport(JSONObject params);
	
	JSONObject getCustomExamStudentScoreReport(JSONObject params);
	
	JSONObject getClassReportExamList(JSONObject params);
	
	JSONObject getSchoolExamClassScoreReport(JSONObject params);
	
	List<JSONObject> getViewScoreParentList(JSONObject params);
	
	List<JSONObject> getClassExamClassScoreReport(JSONObject params);
	
	JSONObject getGradeExamScore(JSONObject params);
	JSONObject getStudentList(JSONObject params);
	JSONObject getExamStudentScoreReport(JSONObject params);
	
	DegreeInfo getDegreeInfoById(String termInfo, Map<String, Object> map);
	
}
