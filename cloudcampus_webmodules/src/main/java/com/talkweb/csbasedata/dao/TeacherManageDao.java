package com.talkweb.csbasedata.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface TeacherManageDao {

	List<JSONObject> getTeacherList(JSONObject param);
	
	String getAccountNameById(long account_id);
	
	int resetTeacherPassword(JSONObject param);

	int addAccountByTeacher(JSONObject param);
	
	int updateAccountByTeacher(JSONObject param);

	int addUserByTeacher(JSONObject param);

	int addTeacher(JSONObject param);
	
	int addTeacherCourses(List<JSONObject> list);

	JSONObject getTeacherInfo(JSONObject param);
	
	JSONObject getNamesFromAccount(JSONObject obj);

	int getTeacherTotal(long schoolId);

	int deleteTeacherCourse(JSONObject param);

	List<JSONObject> getNamesMapBySchoolId(long schoolId);

	void deleteInfoByAccountIds(List<Long> list);

	void deleteInfoByTeacherIds(List<Long> list);

	List<JSONObject> getTeacherListBySchoolId(JSONObject param);

	int insertImportTeacherBatch(Map<String, Object> needInsert);

	List<JSONObject> getAccountObj(JSONObject json);

}