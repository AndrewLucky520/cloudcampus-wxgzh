package com.talkweb.questionnaire.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.User;

public interface AppQuestionnaireDao {
	
	User getUserById(long schoolId, long userId);
	
	List<JSONObject> queryAppQuestionnairesByUser(Map<String, Object> params);
	
	JSONObject queryRuleFortable(Map<String, Object> params);
	
	/**
	 * 查询表格题指标
	 * @param params
	 * @return
	 */
	List<JSONObject> queryOptionFortable(Map<String, Object> params);
	
	/**
	 * 查询个性题指标
	 * @param params
	 * @return
	 */
	List<JSONObject> queryTargetForSpecific(Map<String, Object> params);
	
	/**
	 * 通过班级Id查询年级信息
	 * @param classId
	 * @param schoolId
	 * @param termInfo
	 * @return
	 */
	JSONObject getGradeByClassId(long classId, long schoolId, String termInfo);
	
	/**
	 * 获取当前学年学期
	 * @param schoolId
	 * @return
	 */
	String getCurTermInfoId(long schoolId);
	
	/**
	 * 获取记录id
	 * @param params
	 * @return
	 */
	String getRecordIdByParams(Map<String, Object> params);
	
	/**
	 * 插入记录
	 * @param params
	 * @return
	 */
	int insertRecord(Map<String, Object> params);
	
	/**
	 * 插入详细记录
	 * @param params
	 * @return
	 */
	int insertRecordDetailForTable(Map<String, Object> params);
	
	/**
	 * 删除表格题详细记录
	 * @param params
	 * @return
	 */
	int deleteRecordDetailForTable(Map<String, Object> params);
	
	/**
	 * 删除个性题详细记录
	 * @param params
	 * @return
	 */
	int deleteAppRecordDetailForSpecific(Map<String, Object> params);
	
	/**
	 * 插入个性题详细记录
	 * @param params
	 * @return
	 */
	int insertAppRecordDetailForSpecific(JSONArray array, String recordId, long schoolId);
	
	int deleteAppRecord(JSONObject param);
	
	List<JSONObject> getRecorddetailfortable(Map<String, Object> params);
}
