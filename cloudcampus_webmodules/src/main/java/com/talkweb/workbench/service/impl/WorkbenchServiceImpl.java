package com.talkweb.workbench.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.workbench.dao.WorkbenchDao;
import com.talkweb.workbench.service.PendingItem;
import com.talkweb.workbench.service.WorkbenchService;
@Service
public class WorkbenchServiceImpl  implements WorkbenchService {
	private static final Logger logger = LoggerFactory.getLogger(WorkbenchServiceImpl.class);
	@Autowired
	WorkbenchDao wbDao;
	@Override
	public List<JSONObject> getNavs(JSONObject param) {
			return wbDao.selectNav(param);
	}

	@Override
	public int setFreMenu(JSONObject param) {
		// TODO Auto-generated method stub
		int rc = wbDao.deleteCommonNav(param);
		if(param.getJSONArray("navIds") != null && param.getJSONArray("navIds").size()!=0){
			rc = wbDao.addCommonNav(param);
		}
		return rc;
	}

	@Override
	public List<PendingItem> getPendingItems(JSONObject param) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int addMessage(JSONObject param) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int removeMessage(JSONObject param) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<JSONObject> getMessages(JSONObject param) {
		// TODO Auto-generated method stub
		return null;
	}
	 
	

}
