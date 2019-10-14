package com.talkweb.http.service;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
/***
 * 访问远程接口类
 * @author administator
 *
 */
public interface CallRemoteInterface {
	public String updateHttpRemoteInterface(String url,Map<String, Object> param);
	public String updateHttpRemoteInterface(String url,JSONObject jsonData);
	public String HttpGet(String url);
}
