package com.talkweb.workbench.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface WorkbenchDao {
	int deleteCommonNav(JSONObject param);
	int addCommonNav(JSONObject param);
	List<JSONObject> selectNav(JSONObject param);
	void addNavSchoolBatch(List<JSONObject> list);
	
}
