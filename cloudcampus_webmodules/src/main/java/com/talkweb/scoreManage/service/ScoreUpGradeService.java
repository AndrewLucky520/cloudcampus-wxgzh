package com.talkweb.scoreManage.service;

import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface ScoreUpGradeService {

	/**
	 * 获取全部成绩
	 * 
	 * @return
	 */
	List<JSONObject> getAllKslc();

	/**
	 * 获取本次考试统计科目的代码--暂不实现
	 * 
	 * @param cxMap
	 * @return
	 */
	List<String> getKmdmIntj(HashMap<String, Object> cxMap);

	/**
	 * 学号-班号映射
	 * 
	 * @param cxMap
	 * @return
	 */
	HashMap<String, JSONObject> getXhBhMapByScore(HashMap<String, Object> cxMap);

	/**
	 * 班号-班级统计结果映射
	 * 
	 * @param cxMap
	 * @return
	 */
	HashMap<String, JSONObject> getBjBzfObj(HashMap<String, Object> cxMap);

	/**
	 * 班号_科目-班级统计结果映射
	 * 
	 * @param cxMap
	 * @return
	 */
	HashMap<String, JSONObject> getBjKmBzfObj(HashMap<String, Object> cxMap);

	/**
	 * 更新升级结果集
	 * 
	 * @param cxMap
	 * @throws Exception
	 */
	void updateUpGradeResult(HashMap<String, Object> cxMap) throws Exception;

	void deleteAllStuBzfRes();

}
