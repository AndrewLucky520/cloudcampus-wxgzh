package com.talkweb.ueditor.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface UEditorService {

	List<JSONObject> getUEditorList(JSONObject param);
	int insertUEditorRecord(JSONObject param);
	
}
