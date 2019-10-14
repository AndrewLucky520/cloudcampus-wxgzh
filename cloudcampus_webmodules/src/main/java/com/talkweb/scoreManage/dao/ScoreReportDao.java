package com.talkweb.scoreManage.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface ScoreReportDao {
	boolean ifExistsDegreeInfoByStudId(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsClassExamInfoByStudId(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsCustomClassExamInfoByStudId(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getStudentClassExamByExamId(String termInfoId, Integer autoIncr, Map<String, Object> map);

	JSONObject getStudentAnalysisReport(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getSchoolExamSubjectScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getScoreReportTypeList(Map<String, Object> map);
	
	Integer getNjpmMax(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<String> getKmdmListFromScoretjmk(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getBjcjMk(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getBjcjXs(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getNoExamStudentList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getBjcjCj(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getClassScoreStudentList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getClassScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getClassScores(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getAssignLevelStudentNum(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getCompetitionStuStatistics(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getCompetitionStu(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> getStudentAllExamSubjectList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getCompetitionStuExamScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getSubjectScoreClassStatistics(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getSubjectTotalScoreStatistics(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getStudentScores(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getExamineNameList(String termInfoId, Map<String, Object> map);
	
	List<JSONObject> getClassAverageScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	Integer getExamNormalSubjectCount(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getLevelStudentNumList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	JSONObject getClassReportList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getStudentAllExamScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	JSONObject getARnj(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> getExamSubjectList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	int insertStudentScoreReportTrace(String termInfoId, Integer autoIncr, Object obj);
	
	List<Long> getSubjIdFromCompetitionStu(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getStuSubjectScoreRank_ScoreReport(String termInfoId, Integer autoIncr, Map<String, Object> map);

	JSONObject getSchedule(Map<String,Object> param);
	
	//获取最近的几次考试
	List<JSONObject>  getRecentExams(String termInfoId, Integer autoIncr,  Map<String, Object> map);
	Integer getScoreinfoCnt(String termInfoId, Integer autoIncr,  Map<String, Object> map);
	JSONObject getRecentClassExamInfo(String termInfoId, Integer autoIncr,  Map<String, Object> map);
	List<JSONObject> getRecentStuExamInfo(String termInfoId, Integer autoIncr,  Map<String, Object> map);
	JSONObject getStudentStatisticsrank(String termInfoId, Integer autoIncr,  Map<String, Object> map);
	
	List<JSONObject> getExamListBynj(String termInfoId, Integer autoIncr,  Map<String, Object> map);
	List<JSONObject> getBmfzByClassId(String termInfoId, Integer autoIncr,  Map<String, Object> map);
	List<JSONObject> getSubjectfullScoreByfzdm(String termInfoId, Integer autoIncr,  Map<String, Object> map);

	List<JSONObject> getAllHisSubjectAverageScore(String termInfoId, Integer autoIncr,  Map<String, Object> map);
	
	List<JSONObject> getExamClassReportList(String termInfoId, Integer autoIncr, Map<String, Object> map);
}
