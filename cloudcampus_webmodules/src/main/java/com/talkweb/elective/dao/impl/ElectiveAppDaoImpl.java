package com.talkweb.elective.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.elective.dao.ElectiveAppDao;
@Repository
public class ElectiveAppDaoImpl extends MyBatisBaseDaoImpl  implements ElectiveAppDao {

	@Override
	public List<String> getOpenTimeElectiveApp(JSONObject param) {
		return selectList("getOpenTimeElectiveApp",param);
	}

	@Override
	public JSONObject getElectiveApp(JSONObject param) {
		return selectOne("getElectiveApp",param);
	}

	@Override
	public List<JSONObject> getSelectedElectiveCourseApp(JSONObject param) {
		return selectList("getSelectedElectiveCourseApp",param);
	}

	@Override
	public JSONObject getSingleElectiveCourseApp(JSONObject param) {
		return selectOne("getSingleElectiveCourseApp",param);
	}

	@Override
	public JSONObject getSingleElectiveApp(JSONObject param) {
		return selectOne("getSingleElectiveApp",param);
	}

	@Override
	public JSONObject getSingleSelectedCourseStudent(HashMap<String, Object> map) {
		return  selectOne("getSingleSelectedCourseStudent",map);
	}

	@Override
	public JSONObject getElectiveTimeByIdApp(JSONObject param) {
		return selectOne("getElectiveTimeByIdApp",param);
	}

}
