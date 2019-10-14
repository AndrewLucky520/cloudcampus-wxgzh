package com.talkweb.csbasedata.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface ClassManageService {
	
	int getClassTotal(JSONObject param);

	/** -----查询班级列表----- **/
	List<JSONObject> getClassList(JSONObject param);

	/** -----批量创建班级----- **/
	int batchCreateClass(JSONObject param);

	/** -----创建单个班级----- **/
	int createClass(JSONObject param);

	/** -----查询班级详细信息----- **/
	JSONObject getClassInfo(JSONObject param);

	/** ----更新/编辑班级信息---- **/
	int updateClass(JSONObject param);

	/** -----删除班级----- **/
	void deleteClass(JSONObject param);

	/** -----删除班级任教关系----- **/
	int deleteTeacherCourse(JSONObject param);

	/** -----更新班级任教关系----- **/
	void updateTeacherCourse(JSONObject param);

}