package com.talkweb.scoreManage.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface ScoreDistributionReportDao {
	// 总分分数段报表
	List<JSONObject> getScoreClassDistributeList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getScoreClassStatisticsList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getTotalScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	// 单科分数段报表
	List<JSONObject> getKMScoreDistributeNumList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getKMScoreClassStatisticList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getKMScoreDistributeList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getScoreGroupStatisticsList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	// 名次分段报表
	List<JSONObject> getScoreRankStatisticsList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getRangeNumList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getScoreRankDistributeList(String termInfoId, Integer autoIncr, Map<String, Object> map);
}
