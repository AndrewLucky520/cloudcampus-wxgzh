package com.talkweb.scoreManage.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;

@Service
public interface ScoreReportService {
	
	List<JSONObject> getScoreReportViewList(JSONObject params) throws Exception;
	
	JSONObject getStudentScoreReportList(JSONObject params);
	
	void insertStudentScoreReportTrace(JSONObject params);
	
	JSONArray getScoreReportTypeList(String schooId, String stateType, String roleId);
	
	List<JSONObject> getExamSubjectList(JSONObject params);
	
	JSONObject getClassScoreReportInfo(JSONObject params);
	
	JSONObject getGradeReportList(JSONObject params);
	
	JSONObject getStudentOptimizationList(JSONObject params);
	
	JSONObject getAllPreviousLevelCompareList(School school, JSONObject params);
	
	JSONObject getCompetiteStuAnalysisList(JSONObject params);
	
	JSONObject getSubjectTopNStatisList(School school, JSONObject params);
	
	List<JSONObject> getExamNameList(JSONObject params);
	
	List<JSONObject> getStatisTypeList(JSONObject params);
	
	List<JSONObject> getAllPreviousTrendList(JSONObject params);
	
	JSONObject getClassReportList(JSONObject params);

	JSONObject getStudentScoreReportListByTea(JSONObject params);
	
	JSONObject getStudentOptimization(JSONObject params);
	
	
	JSONObject getRecentClassExamInfo(JSONObject params);
	List<JSONObject> getRecentStuExamInfo(List<JSONObject> data , Long xh);
	
	JSONObject getTeacherScoreAnalysis(JSONObject params);
	JSONObject getStudentScoreAnalysis(JSONObject params);
	
	Map<String, List<JSONObject>> getAllHisSubjectfullScore(JSONObject params);
	Map<String, List<JSONObject>> getAllHisSubjectAverageScore(JSONObject params);
	
}
