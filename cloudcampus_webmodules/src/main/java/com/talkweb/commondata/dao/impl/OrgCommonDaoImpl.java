package com.talkweb.commondata.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.commondata.dao.OrgCommonDao;
@Repository
public class OrgCommonDaoImpl extends MyBatisBaseDaoImpl implements OrgCommonDao {

	@Override
	public List<JSONObject> getOrgInfos(JSONObject obj) {
		return selectList("getOrgInfosCOMMON",obj);
	}

	@Override
	public List<JSONObject> getOrgList(JSONObject obj) {
		return selectList("getOrgListCOMMON",obj);
	}

	@Override
	public List<JSONObject> getOrgScopeList(JSONObject obj) {
		return selectList("getOrgScopeListCOMMON",obj);
	}

	@Override
	public List<JSONObject> getOrgLessonList(JSONObject obj) {
		return selectList("getOrgLessonListCOMMON",obj);
	}

	@Override
	public List<JSONObject> getTeacherByLessonAndGradeBatch(JSONObject obj) {
		return selectList("getTeacherByLessonAndGradeBatchCOMMON",obj);
	}

}
