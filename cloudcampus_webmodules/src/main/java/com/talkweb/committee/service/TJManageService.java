package com.talkweb.committee.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface TJManageService {

	int updateStudentInfo(JSONObject param);

	int deleteStudent(JSONObject param);

	JSONObject getStudentList(JSONObject param,int branch);

	List<JSONObject> getStudentIdentityList(JSONObject param);

	List<JSONObject> getTJGradeList(JSONObject param);

	List<JSONObject> getTJClassList(JSONObject param);

}