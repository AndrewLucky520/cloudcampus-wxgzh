package com.talkweb.ueditor.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.ueditor.dao.UEditorDao;
 

@Repository
public class UEditorDaoImpl extends MyBatisBaseDaoImpl implements UEditorDao{

	@Override
	public List<JSONObject> getUEditorList(JSONObject param) {
		
		return selectList("getUEditorList",param);
	}

	@Override
	public int insertUEditorRecord(JSONObject param) {
		 
		return update("insertUEditor",param);
	}

}
