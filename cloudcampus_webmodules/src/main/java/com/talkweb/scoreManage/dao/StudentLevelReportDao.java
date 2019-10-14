package com.talkweb.scoreManage.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface StudentLevelReportDao {
	Integer getNjpmMax(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getBjcjMk(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsBjcjzhcj(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> getbjcjqamk00(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> getbjcjqamk01(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getbjcjxsdd(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getNjpmInDbKslc(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getbjcjcjdd(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> getGetAllKmdm(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getGetBrkm(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getAllTdTj(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> getAllKmFromStatisticsRankMk(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getEverySubjectScore(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> sql_ddzftjb(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> sql_getAllBj(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<String> sql_getAllDjxlInfo(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getAllZfxl(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getNoA(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getZfJg(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getAllKskm(String termInfoId, Integer autoIncr, Map<String, Object> map);
}
