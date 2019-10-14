package com.talkweb.community.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface CommunityDao {

	List<JSONObject> getCommunities(JSONObject params);

	int insertCommunity(JSONObject params);

	int updateCommunity(JSONObject params);

	int insertAction(JSONObject params);

	int updateAction(JSONObject params);

	int deleteAction(JSONObject params);

	List<JSONObject> selectStudents(JSONObject params);

	List<JSONObject> getActions(JSONObject params);

	int deleteCommunity(JSONObject params);


}
