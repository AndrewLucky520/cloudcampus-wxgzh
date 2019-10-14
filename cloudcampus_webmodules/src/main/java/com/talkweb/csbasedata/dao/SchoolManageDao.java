package com.talkweb.csbasedata.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface SchoolManageDao {
	
	int updateSchoolInfo(JSONObject param);

	JSONObject getSchoolInfo(long schoolId);

	int deleteSchoolStage(String schoolId);

	int insertSchoolStage(List<JSONObject> list);

}