package com.talkweb.scoreManage.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/***
 * 成绩报表管理配置服务
 * 
 * @author guoyuanbing
 * @time 2015-05-28
 */
@Service
public interface ScoreManageConfigService {
	JSONArray getScoreReportNameList(String schoolId, String studyState, String curRole);

	Map<String, Object> updateScoreReportIsShow(List<String> reprotNoList, String schoolId, String stateType, int flag);

	JSONObject getReportConfigs(String reportNo, String schoolId);

	Map<String, Object> saveReportConfigs(String schoolId, String reportNo, String reportName, String config,
			String availableRole);

	JSONObject getReportFieldAuths(String schoolId, String reportNo);

	Map<String, Object> saveReportConfigsCustom(String rptdm, String schoolId, String reportNo, String reportName,
			String config, String availableRole);

	JSONObject getReportConfigsCustom(String reportNo, String schoolId);

	JSONObject delCustomConfig(String reportNo, String schoolId);
}
