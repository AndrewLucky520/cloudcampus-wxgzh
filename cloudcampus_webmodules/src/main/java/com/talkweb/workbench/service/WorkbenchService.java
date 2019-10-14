package com.talkweb.workbench.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface WorkbenchService {
	List<JSONObject> getNavs(JSONObject param);
	int setFreMenu(JSONObject param);
	List<PendingItem> getPendingItems(JSONObject param);
	int addMessage(JSONObject param);
	int removeMessage(JSONObject param);
	List<JSONObject> getMessages(JSONObject param);
}
 