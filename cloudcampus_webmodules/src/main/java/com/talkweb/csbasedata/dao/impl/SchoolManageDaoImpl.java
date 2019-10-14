package com.talkweb.csbasedata.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.csbasedata.dao.SchoolManageDao;

@Repository
public class SchoolManageDaoImpl extends MyBatisBaseDaoImpl implements
		SchoolManageDao {
	
	@Override
	public int updateSchoolInfo(JSONObject param) {
		return update("updateSchoolInfo",param);
	}

	@Override
	public JSONObject getSchoolInfo(long schoolId) {
		return selectOne("getSchoolInfo",schoolId);
	}

	@Override
	public int deleteSchoolStage(String schoolId) {
		return delete("deleteSchoolStage",schoolId);
	}

	@Override
	public int insertSchoolStage(List<JSONObject> list) {
		return insert("insertSchoolStage",list);
	}

}