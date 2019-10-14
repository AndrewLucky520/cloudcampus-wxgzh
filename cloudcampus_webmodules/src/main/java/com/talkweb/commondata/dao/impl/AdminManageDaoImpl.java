package com.talkweb.commondata.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.commondata.dao.AdminManageDao;
@Repository
public class AdminManageDaoImpl  extends MyBatisBaseDaoImpl implements AdminManageDao {

	@Override
	public List<JSONObject> getAdministratorSelectList(JSONObject obj) {
		return selectList("getAdministratorSelectListADMIN",obj);
	}

	@Override
	public List<JSONObject> getNavList(JSONObject obj) {
		return selectList("getNavListADMIN",obj);
	}

	@Override
	public List<JSONObject> getPermissonspcList(JSONObject obj) {
		return selectList("getPermissonspcListADMIN",obj);
	}

	@Override
	public void deleteSchoolManager(JSONObject obj) {
		update("deleteSchoolManagerADMIN",obj);
	}

	@Override
	public void deleteSchoolManagerUser(JSONObject obj) {
		update("deleteSchoolManagerUserADMIN",obj);
		
	}

	@Override
	public void deletePermissonspc(JSONObject obj) {
		update("deletePermissonspcADMIN",obj);
	}

	/*@Override
	public void deletePermissontype(JSONObject obj) {
		update("deletePermissontypeADMIN",obj);
	}*/

	@Override
	public List<JSONObject> getUserByAccountId(JSONObject obj) {
		return selectList("getUserByAccountIdADMIN",obj);
	}

	@Override
	public void resetAdminPassword(JSONObject obj) {
		update("resetAdminPasswordADMIN",obj);
	}

	@Override
	public JSONObject getAccountObj(JSONObject obj) {
		return selectOne("getAccountObjADMIN",obj);
	}

	@Override
	public void insertPermissionspcBatch(JSONObject  obj) {
		update("insertPermissionspcBatchADMIN",obj);
	}
/*
	@Override
	public void insertPermissiontypeBatch(List<JSONObject> list) {
		update("insertPermissiontypeBatchADMIN",list);
	}*/

	@Override
	public void insertSchoolManager(JSONObject obj) {
		update("insertSchoolManagerADMIN",obj);
	}

	@Override
	public void insertUser(JSONObject obj) {
		update("insertUserADMIN",obj);
	}

	@Override
	public JSONObject getSchoolManager(JSONObject obj) {
		return selectOne("getSchoolManagerADMIN",obj);
	}

	@Override
	public List<JSONObject> getAdministratorList(JSONObject obj) {
		return selectList("getAdministratorListADMIN",obj);
	}

	@Override
	public List<JSONObject> getUserPermissonList(JSONObject obj) {
		return selectList("getUserPermissonListADMIN",obj);
	}

	@Override
	public List<String> getAllUserManagerList(JSONObject obj) {
		return selectList("getAllUserManagerListADMIN",obj);
	}

	@Override
	public JSONObject getNewEntranceSchool(JSONObject obj) {
		return selectOne("getNewEntranceSchoolADMIN",obj);
	}

	@Override
	public void updateSchoolManager(JSONObject obj) {
		update("updateSchoolManagerADMIN",obj);
	}

	@Override
	public List<JSONObject> getAdministratorSelectListByTeacherPermission(JSONObject obj) {
	 	return selectList("getAdministratorSelectListByTeacherPermissionADMIN",obj);
	}

	@Override
	public List<JSONObject> getAdministratorListTeacher(JSONObject obj) {
		return selectList("getAdministratorListTeacherADMIN",obj);
	}

	@Override
	public List<String> getAllUserTeacherList(JSONObject obj) {
		return selectList("getAllUserTeacherListADMIN",obj);
	}

}
