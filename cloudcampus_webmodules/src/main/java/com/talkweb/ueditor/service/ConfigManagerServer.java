package com.talkweb.ueditor.service;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;

 

public interface ConfigManagerServer {

	void initEnv(String rootPath, String uri);
	 boolean valid();
	 JSONObject getAllConfig();
	 Map<String, Object> getConfig(int type) ;
}
