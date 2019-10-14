package com.talkweb.commondata.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.commondata.dao.SchoolPlateDao;
/**
 * @ClassName SchoolPlateDaoImpl 基础数据-学校平台映射DaoImpl实现层
 * @author zhh
 * @version 1.0
 * @Description DaoImpl
 * @date 2017年2月13日
 */
@Repository
public class SchoolPlateDaoImpl  extends MyBatisBaseDaoImpl implements SchoolPlateDao{

	@Override
	public List<JSONObject> getSchoolPlateListBy(JSONObject param) {
		return selectList("getSchoolPlateListBy",param);
	}

	@Override
	public JSONObject getSchoolPlateBySchoolId(JSONObject param) {
		return selectOne("getSchoolPlateBySchoolId",param);
	}

	@Override
	public void addSchoolPlate(JSONObject param) {
		 update("addSchoolPlate",param);
	}

	@Override
	public void deleteSchoolPlate(JSONObject param) {
		 update("deleteSchoolPlate",param);
	}

	@Override
	public void updateSchoolPlate(JSONObject param) {
		 update("updateSchoolPlate",param);
	}

}
