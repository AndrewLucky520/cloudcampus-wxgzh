package com.talkweb.scoreManage.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface ViewClassScoreService {
	List<JSONObject> getClassInfoDropDownList(JSONObject params);
	
	JSONObject getClassExamScore(JSONObject params);
	
	List<JSONObject> getClassExamListByTeacher(JSONObject params);
	
	List<JSONObject> getClassListByTeacher(JSONObject params);
}
