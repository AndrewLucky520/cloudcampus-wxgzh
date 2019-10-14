package com.talkweb.ueditor.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.ueditor.dao.UEditorDao;
import com.talkweb.ueditor.service.UEditorService;

@Service
public class UEditorServiceImpl implements UEditorService{

	@Autowired
	UEditorDao ueditorDao;
	@Override
	public List<JSONObject> getUEditorList(JSONObject param) {
		 
		return ueditorDao.getUEditorList(param);
	}

	@Override
	public int insertUEditorRecord(JSONObject oneRecord) {

		return ueditorDao.insertUEditorRecord(oneRecord);
	}

}
