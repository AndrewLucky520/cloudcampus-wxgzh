package com.talkweb.csbasedata.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.csbasedata.dao.CommonManageDao;

@Repository
public class CommonManageDaoImpl extends MyBatisBaseDaoImpl implements
		CommonManageDao {

	@Override
	public List<JSONObject> getGradeBySchoolId(JSONObject param) {
		return selectList("getGradeBySchoolId", param);
	}

	@Override
	public List<JSONObject> getClassByGradeId(JSONObject param) {
		return selectList("getClassByGradeId", param);
	}

	@Override
	public List<JSONObject> getLessonBySchoolId(JSONObject param) {
		return selectList("getLessonBySchoolId", param);
	}

	@Override
	public List<JSONObject> getTeacherByName(JSONObject param) {
		return selectList("getTeacherByName", param);
	}

	@Override
	public String getGradeNameByGradeId(JSONObject param) {	
		String gradeName = "";
		int njdm = selectOne("getCurrentLevelByGradeId",param);
		T_GradeLevel tgl = T_GradeLevel.findByValue(njdm);
		if (null != tgl && AccountStructConstants.T_GradeLevelName.containsKey(tgl)) 
		{
		    gradeName = AccountStructConstants.T_GradeLevelName.get(tgl);
		}
		return gradeName;
	}

	@Override
	public String getClassNameByClassId(long classId) {
		return selectOne("getClassNameByClassId",classId);
	}

	@Override
	public String getTeacherNameByTeacherId(long accountId) {
		return selectOne("getTeacherNameByTeacherId",accountId);
	}

	@Override
	public String getLessonNameByLessonId(long lessonId) {
		return selectOne("getLessonNameByLessonId",lessonId);
	}

	@Override
	public void toActive(JSONObject param) {
		update("updateAccountToActive",param);
	}

}