package com.talkweb.csbasedata.service;

import com.alibaba.fastjson.JSONObject;

public interface SchoolManageService {
	
	/** -----更新学校详细信息----- **/
	void updateSchoolInfo(JSONObject param);

	/** -----查询学校详细信息----- **/
	JSONObject getSchoolInfo(String schoolId);

}