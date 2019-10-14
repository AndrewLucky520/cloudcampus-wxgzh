package com.talkweb.csbasedata.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface SchoolOMSManageService {
	List<JSONObject> getSelectAreaCodeList(JSONObject obj)throws Exception;

	JSONObject getSchoolInfo(JSONObject param)throws Exception;

	int updateSchoolInfo(JSONObject param)throws Exception;
}
