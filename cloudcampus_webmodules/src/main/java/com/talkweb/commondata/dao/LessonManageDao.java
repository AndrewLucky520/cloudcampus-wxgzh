package com.talkweb.commondata.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface LessonManageDao {

	List<JSONObject> getLessonList(String schoolId, String termInfoId);

	int updateLesson(JSONObject param);

	int deleteLesson(JSONObject json);

	int insertLesson(JSONObject param);

	int insertSchoolLesson(JSONObject param);

	List<JSONObject> getLessonByName(JSONObject param);

}