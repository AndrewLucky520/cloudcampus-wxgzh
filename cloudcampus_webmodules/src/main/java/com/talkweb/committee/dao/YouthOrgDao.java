package com.talkweb.committee.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface YouthOrgDao {

	void deleteYouthOrg(JSONObject param);

	List<JSONObject> getYouthOrg(JSONObject param);

	List<JSONObject> getYouthOrgPerson(JSONObject param);

	void addYouthOrgAndPersonBatch(String termInfoId, List<JSONObject> newYouthOrgList,
			List<JSONObject> newYouthOrgList2);

	void addYouthOrg(JSONObject youthOrg);

	void deleteYouthOrgPerson(JSONObject youthOrgPerson);

	void addYouthOrgPersonBatch(List<JSONObject> headsAndMembers);

}
