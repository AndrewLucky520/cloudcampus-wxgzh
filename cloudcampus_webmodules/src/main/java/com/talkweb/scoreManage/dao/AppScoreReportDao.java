package com.talkweb.scoreManage.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;

public interface AppScoreReportDao {
	List<JSONObject> getExamList(String termInfoId, Map<String, Object> map);
	
	boolean ifExistsStuInExamScore(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getClassExamTotalScoreInfoList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getOfficialExamTotalScoreInfoList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getOfficialExamAchievementList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	String getOfficailAppStudentReport(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> selectSchoolExamSubjectScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getFrontFiveSchoolExamList(String termInfoId, Map<String, Object> map);
	
	int insertExamView(String termInfoId, Integer autoIncr, Object obj);
	
	List<JSONObject> selectClassExamEverSubjectScore(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	JSONObject selectClassExamTotalScore(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	JSONObject selectClassExamClassTotalScore(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> selectClassExamSubjectList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> selectCustomScoreByStudentId(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> selectTeacherExamSoreList(String termInfoId, Map<String, Object> map);
	
	List<Long> getBhListFromStatistics(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> getClassIdFromClassExamRelative(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> getClassIdFromCustomScoreInfo(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> selectParentViewList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> selectSchoolExamCount(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> selectClassExamCount(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> selectCustomExamCount(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	String selectClassReportViewList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> selectSchoolExamUser(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> selectClassExamUser(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> selectCustomExamUser(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	Set<Long> selectParentView(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> selectClassExamSubjectScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	JSONObject getGradeExamScore(String termInfoId, Integer autoIncr, Map<String, Object> map);
	List<JSONObject> getStudentList(String termInfoId, Integer autoIncr, JSONObject param);
	List<JSONObject> getExamStudentScoreReport(String termInfoId, Integer autoIncr, JSONObject param);
	
	List<JSONObject> getSchoolExamList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	boolean ifExistsClassOrStuInExamScore(String termInfoId, Integer autoIncr, JSONObject param);
	List<JSONObject> getFullScore (String termInfoId, Integer autoIncr, Map<String, Object> map);
	JSONObject getClassAvergaeScore(String termInfoId, Integer autoIncr, Map<String, Object> map);
	JSONObject getGradeAvergaeScore(String termInfoId, Integer autoIncr, Map<String, Object> map);
}
