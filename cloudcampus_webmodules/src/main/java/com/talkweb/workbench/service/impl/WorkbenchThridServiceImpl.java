package com.talkweb.workbench.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.workbench.dao.WorkbenchDao;
import com.talkweb.workbench.service.WorkbenchThridService;
@Service
public class WorkbenchThridServiceImpl implements WorkbenchThridService {
	private static final Logger logger = LoggerFactory.getLogger(WorkbenchThridServiceImpl.class);
	@Autowired
	WorkbenchDao wbDao;
	
	@Override
	public void addNavSchool(JSONObject param) throws Exception {
		String type = param.getString("type");
		String schoolId = param.getString("schoolId");
		if(StringUtils.isBlank(type)){
			return ;
		}
		List<JSONObject> list = new ArrayList<JSONObject>();
		JSONObject json = new JSONObject();
		json.put("schoolId", schoolId);
		json.put("navTypeId", 1);
		list.add(json);
		if("2".equals(type)){ //新高考学校
			JSONObject json1 = new JSONObject();
			json1.put("schoolId", schoolId);
			json1.put("navTypeId", 2);
			list.add(json1);
		}
		wbDao.addNavSchoolBatch(list);
	}

}
