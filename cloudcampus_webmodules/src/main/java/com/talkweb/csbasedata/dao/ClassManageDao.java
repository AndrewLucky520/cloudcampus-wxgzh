package com.talkweb.csbasedata.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface ClassManageDao {
	
	JSONObject getCountByClassCond(JSONObject param);

	List<JSONObject> getClassList(JSONObject param);
	
	int addClass(JSONObject param);

	int addClassList(List<JSONObject> list);

	JSONObject getClassInfo(JSONObject param);

	int updateClassInfo(JSONObject param);

	int deleteClassCourse(JSONObject param);
	
	int deleteClass(JSONObject param);

	int addClassCourses(List<JSONObject> list);

	List<String> getStagesBySchoolId(String schoolId);

	int deleteExtraGrade(JSONObject param);

	int deleteClassCourses(List<Long> list);

	List<Long> getClassTeacherList(JSONObject param);

	void deleteClassCoursesBatchBy(JSONObject deletedClassCourses);

	List<JSONObject> selectStudentByClassId(JSONObject json);

	void deleteStudentByClassId(JSONObject json);

	void deleteUser(JSONObject json);

	void deleteAccount(JSONObject json);

	List<JSONObject> getParentListByStudentId(JSONObject json);

	void deleteUserExtend(JSONObject json);

}