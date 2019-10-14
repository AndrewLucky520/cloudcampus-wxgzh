package com.talkweb.committee.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.committee.dao.TJManageDao;

@Repository
public class TJManageDaoImpl extends MyBatisBaseDaoImpl implements TJManageDao {

	@Override
	public int updateStudentInfo(JSONObject param) {
		return update("updateTJStudent",param);
	}

	@Override
	public int deleteStudent(JSONObject param) {
		return delete("deleteTJStudent",param);
	}

	@Override
	public List<JSONObject> getStudentList(JSONObject param) {
		return selectList("getTJStudentList",param);
	}

}
