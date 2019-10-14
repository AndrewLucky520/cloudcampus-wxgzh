package com.talkweb.commondata.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.commondata.dao.UpGradeDao;
/**
 * @ClassName UpGradeDaoImpl
 * @author zhanghuihui
 * @version 1.0
 * @Description DaoImpl
 * @date 2017年8月9日
 */
@Repository
public class UpGradeDaoImpl  extends MyBatisBaseDaoImpl implements UpGradeDao{

	@Override
	public List<JSONObject> getTermInfosBySchoolId(JSONObject json) {
		return selectList("getTermInfosBySchoolIdUPGRADE",json);
	}

	@Override
	public List<JSONObject> getTermInfos(JSONObject json) {
		return selectList("getTermInfosUPGRADE",json);
	}

	@Override
	public void deGradeBySchoolId(JSONObject json) {
		update("updateTermInfosUPGRADE",json);
	}

	@Override
	public void upGradeBySchoolId(JSONObject json) {
		update("updateTermInfosUPGRADE",json);
	}

	@Override
	public void deGradeAllSchools(JSONObject json) {
		update("updateTermInfosUPGRADE",json);
	}

	@Override
	public void updateGradeAllSchools(JSONObject json) {
		update("updateTermInfosUPGRADE",json);
	}


}
