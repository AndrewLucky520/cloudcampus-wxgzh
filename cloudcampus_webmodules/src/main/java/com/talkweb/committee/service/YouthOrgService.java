package com.talkweb.committee.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface YouthOrgService {

	List<JSONObject> setGetYouthOrgList(JSONObject param) throws Exception;

	int addYouthOrg(JSONObject param) throws Exception;

	void deleteYouthOrg(JSONObject param) throws Exception;

	JSONObject getYouthOrgDetail(JSONObject param) throws Exception;

}
