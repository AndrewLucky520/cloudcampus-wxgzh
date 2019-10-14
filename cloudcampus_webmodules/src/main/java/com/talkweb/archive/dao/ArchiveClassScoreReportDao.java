package com.talkweb.archive.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface ArchiveClassScoreReportDao {

	List<JSONObject> getTeacherClassScoreAnalyze(String termInfoId, Integer autoIncr, Map<String, Object> map);
	List<JSONObject> selectViewScoreClassGroupClassList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	boolean ifExistsTopGroupData(String termInfoId, Integer autoIncr, Map<String, Object> map);
	List<JSONObject> getStudentScoreAverageValue(String termInfoId, Integer autoIncr, Map<String, Object> map);
	List<JSONObject> getExcellentPassRate(String schoolId);
	
}
