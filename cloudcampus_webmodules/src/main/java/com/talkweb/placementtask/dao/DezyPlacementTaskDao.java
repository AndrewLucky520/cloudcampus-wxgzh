package com.talkweb.placementtask.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.placementtask.domain.TPlDezyAdvancedOpt;
import com.talkweb.placementtask.domain.TPlDezyAutorize;
import com.talkweb.placementtask.domain.TPlDzbClassLevel;

public interface DezyPlacementTaskDao {
	/**
	 * 插入分班预设条件
	 * @param obj
	 * @return
	 */
	int insertDezyPreSetting(Map<String,Object> obj);
	int insertDezySubjectGroup(Map<String,Object> obj);
	int insertDezySubjectSet(Map<String,Object> obj);
	
	/**
	 * 
	 * @param obj{schoolId,termInfo,usedGrade}
	 * @return 分班代码（placementId）
	 */
	String queryLatestDivData(Map<String,Object> obj);
	List<String> queryTableColumnNames(Map<String,Object> obj);
	int batchInsertTPlDezyTableFromItself(Map<String,Object> obj);
	
	/**
	 * 
	 * @param obj:{schooId,placementId}
	 * @return
	 */
	JSONObject queryDezyPreSetting(Map<String,Object> obj);
	
	/**
	 * 查询学校定二走一权限
	 * @param obj：{schoolId}
	 * @return
	 */
	List<TPlDezyAutorize> queryDezyAutorize(Map<String,Object> obj);
	
	/**
	 * 查询实验班
	 * @param obj
	 * @return
	 */
	List<TPlDezyAdvancedOpt> queryDezyAdvancedOpt(Map<String,Object> obj);
	/**
	 * 批量插入 实体（javabean ，不可以是json）
	 * @param dataList
	 * @throws Exception
	 */
	<T> void updateBatchInsertEntity(String termInfo,List<T> dataList) throws Exception;
	
	int recoverClassName(Map<String,Object> obj);
	/**
	 * 清除大走班结果
	 * @param obj
	 */
	void updateClearLargeResult(JSONObject obj);
	
	//-------------------------------------------->>>>  大走班          <<<<--------------------------------------
	JSONObject getDzbPreSettings(Map<String,Object> obj);
	List<TPlDzbClassLevel> getDzbClassLevel(Map<String,Object> obj);
	
	List<JSONObject> getDzbDivQueryParams(Map<String,Object> obj);
	//查询分班预览
	List<JSONObject> getDzbDivResult(Map<String,Object> obj);
	
	//大走班学生分班明细
	List<JSONObject> queryDzbStuClassInfoDetail(Map<String,Object> obj);
	
	//大走班师资详情
	List<JSONObject> queryDzbTeachingResource(Map<String,Object> obj);
	
	//大走班主表
	List<JSONObject> queryDzbMainTable(Map<String,Object> obj);
	
	//大走班分班结果预览
	List<JSONObject> queryDzbPreview(Map<String,Object> obj);
	
	//查找科目对应的教学场地
	List<JSONObject> selectGroundIdBysubjectIds(Map<String,Object> obj);
}
