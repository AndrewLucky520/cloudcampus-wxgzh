package com.talkweb.workbench.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;

@Repository
public class WorkbenchDaoImpl extends MyBatisBaseDaoImpl implements WorkbenchDao {

	@Override
	public int deleteCommonNav(JSONObject param) {
		// TODO Auto-generated method stub
		return this.delete("delComonNavs",param);
	}

	@Override
	public int addCommonNav(JSONObject param) {
		// TODO Auto-generated method stub
		return this.insert("addCommonNavs",param);
	}

	@Override
	public List<JSONObject> selectNav(JSONObject param) {
		// TODO Auto-generated method stub
		return this.selectList("selectNavs",param);
	}

	@Override
	public void addNavSchoolBatch(List<JSONObject> list) {
		 this.insert("addNavSchoolBatch",list);
	}
	
}
