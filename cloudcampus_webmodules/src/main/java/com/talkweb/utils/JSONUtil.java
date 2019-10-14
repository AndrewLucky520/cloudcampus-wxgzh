package com.talkweb.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class JSONUtil {
    private static final Logger log = LoggerFactory.getLogger(JSONUtil.class);
	public static JSONObject getResponse() {
		return getResponse(0, null, null);
	}

	public static JSONObject getResponse(int code) {
		return getResponse(code, null, null);
	}

	public static JSONObject getResponse(int code, String msg) {
		return getResponse(code, msg, null);
	}

	public static JSONObject getResponse(String code, String msg) {
		return getResponse(code, msg, null);
	}

	public static JSONObject getResponse(String msg, Exception ex) {
		log.error("{}",msg, ex);
		return getResponse(-1, msg, null);
	}

	public static JSONObject getResponse(Exception ex, int code, String msg) {
		log.error("{}",msg, ex);
		return getResponse(code, msg, null);
	}

	public static JSONObject getResponse(Object data) {
		return getResponse(0, null, data);
	}

	public static JSONObject getResponse(int code, JSONObject data) {
		return getResponse(code, null, data);
	}

	public static JSONObject getResponse(List<JSONObject> data) {
		return getResponse(0, null, data);
	}

	public static JSONObject getResponse(int code, List<JSONObject> data) {
		return getResponse(code, null, data);
	}

	public static JSONObject getResponse(Object code, String msg, Object data) {
		JSONObject ret = new JSONObject();
		ret.put("code", code);
		if (msg != null) {
			ret.put("msg", msg);
		}
		if (data != null) {
			ret.put("data", data);
		}
		return ret;
	}
}
