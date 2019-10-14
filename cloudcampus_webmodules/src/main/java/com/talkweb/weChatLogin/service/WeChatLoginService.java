package com.talkweb.weChatLogin.service;

import java.util.HashMap;

public interface WeChatLoginService {

	HashMap getUserBySDK(HashMap<String, Object> param);
}
