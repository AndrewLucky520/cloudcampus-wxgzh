package com.talkweb.scoreManage.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.scoreManage.business.ClassScoreInParam;

public interface ClassScoreReportDao {
	List<JSONObject> getClassScores(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getClassScoreAnalyze(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> selectViewScoreClassGroupClassList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	Set<String> getBhFromScoreInfo(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getGradeScoreAnalyze(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getExcellentPassRate(String schoolId);
	
	boolean ifExistsTopGroupData(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getStudentScoreAverageValue(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getAGradeRate(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getAGroupGradeRate(String termInfoId, Integer autoIncr, Map<String, Object> map);
 
	int getTopGourpNumber(ClassScoreInParam param);
	
	List<JSONObject> getClassCourses(ClassScoreInParam param);

	List<JSONObject> getAllIndexClassScoreList(ClassScoreInParam param);

	List<JSONObject> getAllIndexGradeScorelist(ClassScoreInParam param);

	List<JSONObject> getMaxMinScoreByCourseList(ClassScoreInParam param);

	List<JSONObject> getFullRangeEveryTeacherAverageList(ClassScoreInParam param);
	
	JSONObject getDeanScoreAnalyzeGrade(String termInfoId, Integer autoIncr, Map<String, Object> map);
	List<JSONObject> getDeanScoreAnalyzeClass(String termInfoId, Integer autoIncr, Map<String, Object> map);
    JSONObject  getTeacherScoreAnalyzeGrade(String termInfoId, Integer autoIncr, Map<String, Object> map);
    JSONObject getTeacherScoreAnalyzeClass(String termInfoId, Integer autoIncr, Map<String, Object> map);
 
 
	

}
