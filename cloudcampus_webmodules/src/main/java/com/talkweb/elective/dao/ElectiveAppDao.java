package com.talkweb.elective.dao;

import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface ElectiveAppDao {

	List<String> getOpenTimeElectiveApp(JSONObject param);

	JSONObject getElectiveApp(JSONObject param);

	List<JSONObject> getSelectedElectiveCourseApp(JSONObject param);

	JSONObject getSingleElectiveCourseApp(JSONObject param);

	JSONObject getSingleElectiveApp(JSONObject param);

	JSONObject getSingleSelectedCourseStudent(HashMap<String, Object> map);

	JSONObject getElectiveTimeByIdApp(JSONObject param);

	
}
