package com.talkweb.systemManager.service;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONObject;

public interface LoginService {
	public JSONObject setSessionAndGetLoginStatus(HttpSession session,long userId)throws Exception; 
}
