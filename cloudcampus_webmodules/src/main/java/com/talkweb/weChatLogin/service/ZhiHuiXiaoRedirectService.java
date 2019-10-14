package com.talkweb.weChatLogin.service;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

public interface ZhiHuiXiaoRedirectService {

	

	HashMap getUserBySDK(HashMap<String, Object> param,
			HttpServletRequest request);
}
