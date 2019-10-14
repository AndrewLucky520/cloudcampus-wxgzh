package com.talkweb.csbasedata.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface StudentBaseManageDao {

	List<JSONObject> getAccountObj(JSONObject param);

	void resetAdminPassword(JSONObject param);
	
	void deleteAccount(JSONObject param);
	void deleteUser(JSONObject param);
	void deleteParent(JSONObject param);
	void deleteStudent(JSONObject param);

	JSONObject getStudentInfo(JSONObject param);

	JSONObject getStudentGradeAndClass(JSONObject param);

	List<JSONObject> getStudentList(JSONObject param);

	List<JSONObject> getParentList(JSONObject param);

	List<String> getStudentIds(JSONObject param);

	void deleteUserExtend(JSONObject param);
	long insertUser(JSONObject param);
	long insertAccount(JSONObject param);
	void insertParent(JSONObject param);
	void insertStudent(JSONObject param);
	void insertUserExtend(JSONObject param);

	void updateAccountPwd(JSONObject param);

	void updateParentAccount(JSONObject param);

	void updateStudentAccount(JSONObject studentAccountObj);

	void updateStudent(JSONObject student);

	int insertImportStudentBatch(Map<String, Object> needInsert);

	List<JSONObject> getUserExtend(JSONObject ue);

	List<JSONObject> getStudentAccountObj(JSONObject json);

	List<JSONObject> getGradeListBySchoolId(JSONObject obj);

	List<JSONObject> getClassListBySchoolId(JSONObject obj);

	void updateParentClassId(JSONObject param);

	List<JSONObject> getStudentListBySchoolId(JSONObject obj);

	List<JSONObject> getAccountByUserId(JSONObject obj);

	int getStudentTotal(JSONObject param);

	List<JSONObject> getParentStudentList(JSONObject param);

	List<JSONObject> getUserExtendName(JSONObject ue);

	List<JSONObject> getStudentRepeatList(JSONObject sr);
}