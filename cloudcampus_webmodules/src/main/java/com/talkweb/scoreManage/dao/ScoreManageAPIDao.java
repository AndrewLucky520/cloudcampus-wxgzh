package com.talkweb.scoreManage.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface ScoreManageAPIDao {
	List<JSONObject> getScoreIdAndNameList(String termInfoId, JSONObject params);
	
	boolean ifExistsScoreInfo(String termInfoId, Integer autoIncr, JSONObject params);
	
	List<JSONObject> queryScoreInfo(String termInfoId, Integer autoIncr, JSONObject params);
}
