package com.talkweb.scoreManage.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface ScoreManageConfigDao {
	List<JSONObject> getScoreReportNameList(Map<String, Object> map);

	int updateScoreReportIsShow(Map<String, Object> map);

	JSONObject getScoreReportConfig(Map<String, Object> map);

	int addScoreReportConfig(Map<String, Object> map);

	int updateScoreReportConfig(Map<String, Object> map);

	int selectOneScoreReportConfig(Map<String, Object> map);

	JSONObject getScoreReportDefaultConfig(Map<String, Object> map);

	int updateScoreReportInfo(Map<String, Object> map);

	List<JSONObject> getScoreReportConfigCustom(HashMap<String, Object> map);

	JSONObject getScoreReportConfigCustom(Map<String, Object> map);

	int addScoreReportConfigCustom(Map<String, Object> map);

	int delCustomConfig(Map<String, Object> map);

	int updateScoreReportConfigCustom(Map<String, Object> map);
}
