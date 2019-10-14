package com.talkweb.ueditor.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface FileManagerServer {
	 //void setProperty(Map<String, Object> conf);
	 State listFile (List<JSONObject> list,  int index );
}
