package com.talkweb.elective.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface ElectiveAppService {
	List<String> getOpenTimeElectiveApp(JSONObject param) throws Exception;
	JSONObject getResultElectiveCourseApp(JSONObject param)throws Exception;
	JSONObject getElectiveCourseDetailApp(JSONObject param)throws Exception;
	JSONObject getElectiveCourseListApp(JSONObject param)throws Exception;
	int meetElectiveApp(JSONObject param)throws Exception;
	JSONObject getElectiveTimeByIdApp(JSONObject param);
}
