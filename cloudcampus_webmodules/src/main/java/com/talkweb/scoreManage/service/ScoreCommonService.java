package com.talkweb.scoreManage.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface ScoreCommonService {
	List<JSONObject> getExamGradeList(JSONObject params);

	List<JSONObject> getClassList(JSONObject params);

	List<JSONObject> getExamNameList(JSONObject params);

	/**
	 * 公共-获取文理科分组下拉菜单
	 */
	List<JSONObject> getwlkGroupList(JSONObject params);

	/**
	 * 查询文理分组下的班级的下拉菜单
	 */
	List<JSONObject> getClassListByGroupId(JSONObject params);

	List<JSONObject> getExamSubjectDropDownList(JSONObject params);

	List<JSONObject> getExamClassDropDownList(JSONObject params);
	
	List<JSONObject> getExamClassList(JSONObject params);
}
