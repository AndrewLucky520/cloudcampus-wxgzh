package com.talkweb.commondata.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface AdminManageService {
	List<JSONObject> getAdministratorList(JSONObject obj) throws Exception;
	int updateAdministrator(JSONObject obj) throws Exception;
	int resetAdminPassword(JSONObject obj) throws Exception;
	int deleteAdministrator(JSONObject obj) throws Exception;
	List<JSONObject> getAdministratorSelectList(JSONObject obj) throws Exception;
	List<JSONObject> getAdministrator(JSONObject param) throws Exception;
}
