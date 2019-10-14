package com.talkweb.committee.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface TJManageDao {

	int updateStudentInfo(JSONObject param);

	int deleteStudent(JSONObject param);

	List<JSONObject> getStudentList(JSONObject param);

}
