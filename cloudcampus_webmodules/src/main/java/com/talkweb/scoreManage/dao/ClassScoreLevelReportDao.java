package com.talkweb.scoreManage.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface ClassScoreLevelReportDao {
	// 科目等级统计表
	List<JSONObject> getAllDjInfo();

	List<JSONObject> getDDKMJB(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getQnjpjf(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getNumInfo(String termInfoId, Integer autoIncr, Map<String, Object> map);

	// 等第人数统计表
	List<JSONObject> getAllDjxlInfo(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getAllPmqj(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getAllDjxlInformation(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getAllPmInfo(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getNjPmfb(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getNjDjxl(String termInfoId, Integer autoIncr, Map<String, Object> map);
}
