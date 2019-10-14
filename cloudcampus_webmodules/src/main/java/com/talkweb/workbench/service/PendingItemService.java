package com.talkweb.workbench.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.workbench.service.PendingItem.ModualType;

/**
 * 待办事项service
 * @author Administrator
 *
 */
public interface PendingItemService {

	/**
	 * 获取模块类型
	 * @return
	 */
	public ModualType getModualType();
	/**
	 * 获取待办事项
	 * @param JSONObject {userId:xxxx,role:x}
	 * @return PendingItem
	 */
	public List<PendingItem> getPendingItem(JSONObject param);
}
