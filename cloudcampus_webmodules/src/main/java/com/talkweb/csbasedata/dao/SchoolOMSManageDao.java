package com.talkweb.csbasedata.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

//学校管理Dao
public interface SchoolOMSManageDao {
	List<JSONObject> getSelectAreaCodeList(JSONObject param);

	JSONObject getSchoolInfo(JSONObject param);

	void insertSchool(JSONObject param);

	void insertSchoolStageBatch(List<JSONObject> list);

	void deleteSchoolStage(JSONObject param);

	void updateSchool(JSONObject param);

	List<JSONObject> getAccountObj(JSONObject param);

	void updateAccountPwd(JSONObject param);

	long insertAccount(JSONObject param);

	void insertJobtypeBatch(List<JSONObject> list);

	void insertTermInfo(JSONObject param);

	void insertJobTypeBatch(List<JSONObject> list);

	void insertSchoolLessonBatch(List<JSONObject> list);


	void deleteSchoolLesson(JSONObject param);

	void deleteSchoolGradeStage(JSONObject param);

	void insertSchoolGradeStage(JSONObject param);

	void deleteTermInfo(JSONObject param);

	List<JSONObject> getSchoolByName(JSONObject param);

	List<JSONObject> getSchoolManagerAccountObj(JSONObject json);

	List<JSONObject> getUserByAccountId(JSONObject userObj);

	List<JSONObject> getOrgListByUUID(JSONObject json);

	void insertOrgTypeBatch(List<JSONObject> orgTypeList);

	void insertGradeBatch(List<JSONObject> gradeList);

	void insertSchoolPlate(JSONObject schoolPlate);
}
