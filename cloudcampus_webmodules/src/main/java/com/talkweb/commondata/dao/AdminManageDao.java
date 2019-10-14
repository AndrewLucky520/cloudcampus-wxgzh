package com.talkweb.commondata.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface AdminManageDao {

	List<JSONObject> getAdministratorSelectList(JSONObject obj);
	List<JSONObject> getNavList(JSONObject obj);
	List<JSONObject> getPermissonspcList(JSONObject obj);
	void deleteSchoolManager(JSONObject obj);
	void deleteSchoolManagerUser(JSONObject obj);
	void deletePermissonspc(JSONObject obj);
	//void deletePermissontype(JSONObject obj);
	List<JSONObject> getUserByAccountId(JSONObject obj);
	void resetAdminPassword(JSONObject obj);
	JSONObject getAccountObj(JSONObject obj);
	void insertPermissionspcBatch(JSONObject json);
	//void insertPermissiontypeBatch(List<JSONObject> list);
	void insertSchoolManager(JSONObject obj);
	void insertUser(JSONObject obj);
	JSONObject getSchoolManager(JSONObject obj);
	List<JSONObject> getAdministratorList(JSONObject obj);
	List<JSONObject> getUserPermissonList(JSONObject obj);
	List<String> getAllUserManagerList(JSONObject obj);
	JSONObject getNewEntranceSchool(JSONObject obj);
	void updateSchoolManager(JSONObject obj);
	List<JSONObject> getAdministratorSelectListByTeacherPermission(JSONObject obj);
	List<JSONObject> getAdministratorListTeacher(JSONObject obj);
	List<String> getAllUserTeacherList(JSONObject obj);
}
