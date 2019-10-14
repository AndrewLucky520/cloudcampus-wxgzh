package com.talkweb.community.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface CommunityService {
	public List<JSONObject> getCommunitys(JSONObject params);
	public int insertCommunity(JSONObject params);
	public int updateCommunity(JSONObject params);
	public int deleteCommunity(JSONObject params);
	public List<JSONObject> getActions(JSONObject params);
	public int insertAction(JSONObject params);
	public int updateAction(JSONObject params);
	public int deleteAction(JSONObject params);
	public List<JSONObject> selectStudents(JSONObject params);


}
