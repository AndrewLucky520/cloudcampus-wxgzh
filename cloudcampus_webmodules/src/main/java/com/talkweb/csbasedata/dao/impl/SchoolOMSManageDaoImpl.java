package com.talkweb.csbasedata.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.csbasedata.dao.SchoolOMSManageDao;
@Repository
public class SchoolOMSManageDaoImpl  extends MyBatisBaseDaoImpl implements SchoolOMSManageDao {

	@Override
	public List<JSONObject> getSelectAreaCodeList(JSONObject param) {
		return selectList("getSelectAreaCodeListOMS",param);
	}

	@Override
	public JSONObject getSchoolInfo(JSONObject param) {
		return selectOne("getSchoolInfoOMS",param);
	}

	@Override
	public void insertSchool(JSONObject param) {
		update("insertSchoolOMS",param);
	}

	@Override
	public void insertSchoolStageBatch(List<JSONObject> list) {
		update("insertSchoolStageBatchOMS",list);
	}

	@Override
	public void deleteSchoolStage(JSONObject param) {
		update("deleteSchoolStageOMS",param);
	}

	@Override
	public void updateSchool(JSONObject param) {
		update("updateSchoolOMS",param);
	}

	@Override
	public List<JSONObject> getAccountObj(JSONObject param) {
		return selectList("getAccountObjOMS",param);
	}

	@Override
	public void updateAccountPwd(JSONObject param) {
		update("updateAccountPwdOMS",param);
	}

	@Override
	public long insertAccount(JSONObject param) {
		return update("insertAccountOMS",param);
	}

	@Override
	public void insertJobtypeBatch(List<JSONObject> list) {
		update("insertJobtypeBatchOMS",list);
	}

	@Override
	public void insertTermInfo(JSONObject param) {
		update("insertTermInfoOMS",param);
	}

	@Override
	public void insertJobTypeBatch(List<JSONObject> list) {
		update("insertJobTypeBatchOMS",list);
	}

	@Override
	public void insertSchoolLessonBatch(List<JSONObject> list) {
		update("insertSchoolLessonBatchOMS",list);
	}


	@Override
	public void insertSchoolGradeStage(JSONObject param) {
		update("insertSchoolGradeStageOMS",param);
	}

	@Override
	public void deleteSchoolLesson(JSONObject param) {
		update("deleteSchoolLessonOMS",param);
	}

	@Override
	public void deleteSchoolGradeStage(JSONObject param) {
		update("deleteSchoolGradeStageOMS",param);
	}

	@Override
	public void deleteTermInfo(JSONObject param) {
		update("deleteTermInfoOMS",param);
	}

	@Override
	public List<JSONObject> getSchoolByName(JSONObject param) {
		return selectList("getSchoolByNameOMS",param);
	}

	@Override
	public List<JSONObject> getSchoolManagerAccountObj(JSONObject json) {
		return selectList("getSchoolManagerAccountObjOMS",json);
	}

	@Override
	public List<JSONObject> getUserByAccountId(JSONObject userObj) {
		return selectList("getUserByAccountIdOMS",userObj);
	}

	@Override
	public List<JSONObject> getOrgListByUUID(JSONObject json) {
		return selectList("getOrgListByUUIDOMS",json);
	}

	@Override
	public void insertOrgTypeBatch(List<JSONObject> orgTypeList) {
		update("insertOrgTypeBatchOMS",orgTypeList);
	}

	@Override
	public void insertGradeBatch(List<JSONObject> gradeList) {
		update("insertGradeBatchOMS",gradeList);
	}

	@Override
	public void insertSchoolPlate(JSONObject schoolPlate) {
		update("insertSchoolPlateOMS",schoolPlate);
	}
}
