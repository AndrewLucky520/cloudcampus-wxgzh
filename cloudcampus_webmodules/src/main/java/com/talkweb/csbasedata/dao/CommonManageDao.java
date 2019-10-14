package com.talkweb.csbasedata.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface CommonManageDao {

	List<JSONObject> getGradeBySchoolId(JSONObject param);

	List<JSONObject> getClassByGradeId(JSONObject param);

	List<JSONObject> getLessonBySchoolId(JSONObject param);

	List<JSONObject> getTeacherByName(JSONObject param);
	
	String getGradeNameByGradeId(JSONObject param);
	
	String getClassNameByClassId(long classId);

	String getTeacherNameByTeacherId(long teacherId);
	
	String getLessonNameByLessonId(long lessonId);

	void toActive(JSONObject param);

}