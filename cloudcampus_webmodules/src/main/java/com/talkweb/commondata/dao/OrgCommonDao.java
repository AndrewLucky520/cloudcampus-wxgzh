package com.talkweb.commondata.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface OrgCommonDao {

	List<JSONObject> getOrgInfos(JSONObject obj);

	List<JSONObject> getOrgList(JSONObject obj);

	List<JSONObject> getOrgScopeList(JSONObject obj);

	List<JSONObject> getOrgLessonList(JSONObject obj);

	List<JSONObject> getTeacherByLessonAndGradeBatch(JSONObject obj);

}
