package com.talkweb.csbasedata.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public interface TeacherManageService {

	/** -----查询教师列表----- **/
	List<JSONObject> getTeacherList(JSONObject param)throws Exception;
	
	/** -----增加教师----- **/
	int addTeacher(JSONObject param)throws Exception;

	/** -----更新教师信息----- **/
	int updateTeacher(JSONObject param)throws Exception;

	/** -----重置老师密码----- **/
	int resetTeacherPassword(String accountId)throws Exception;

	/** -----查询教师详细信息----- **/
	JSONObject getTeacherInfo(JSONObject param)throws Exception;

	/** -----删除老师信息----- **/
	void deleteTeacher(JSONArray param)throws Exception;

	/** -----查询教师数目----- **/
	int getTeacherTotal(long schoolId)throws Exception;

	/** -----删除教师任教关系----- **/
	int deleteTeacherCourse(JSONObject param)throws Exception;

	/** -----更新老师任教关系----- **/
	void updateTeacherCourses(JSONObject param)throws Exception;

	int insertImportTeacherBatch(Map<String, Object> needInsert)throws Exception;

	

}