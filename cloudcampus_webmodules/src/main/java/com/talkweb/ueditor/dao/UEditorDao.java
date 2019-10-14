package com.talkweb.ueditor.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface UEditorDao {
	List<JSONObject> getUEditorList(JSONObject param);
	int insertUEditorRecord(JSONObject param);
}


