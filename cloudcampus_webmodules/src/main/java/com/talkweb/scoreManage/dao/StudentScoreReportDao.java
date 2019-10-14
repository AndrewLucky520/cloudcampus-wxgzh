package com.talkweb.scoreManage.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface StudentScoreReportDao {
	List<JSONObject> getbjcjgzxs(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getbjcjgzsmxx(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	Map<String, List<String>> getDbxnxq2KslcMap(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> getBjcjMkGZ(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getbjcjgzcj(String termInfoId, Integer autoIncr, Map<String, Object> map);
}
