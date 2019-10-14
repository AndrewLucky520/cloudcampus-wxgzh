package com.talkweb.workbench.service;

import com.alibaba.fastjson.JSONObject;

public interface WorkbenchThridService {
	/**
	 * 添加默认菜单
	 * @param param   schoolId必填 
	 *                type : 1 非新高考学校，2 新高考学校
	 * @throws Exception
	 */
	void addNavSchool( JSONObject param )throws Exception;
}
