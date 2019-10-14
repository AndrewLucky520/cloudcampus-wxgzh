package com.talkweb.csbasedata.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
//机构管理-dao
public interface OrgManageDao {
	//机构
	List<JSONObject> getOrgList(JSONObject obj);
	int updateOrg(JSONObject obj);
	List<JSONObject> getOrgInfos(JSONObject obj);
	int deleteOrg(JSONObject obj);
	int deleteOrgMember(JSONObject obj);
	int deleteOrgLeader(JSONObject obj);
	List<JSONObject> getUserIdByAccountIdList(JSONObject obj);
	void insertOrgMemberBatch(List<JSONObject> ms);
	void insertOrgLeaderBatch(List<JSONObject> ls);
	void insertOrg(JSONObject obj);
	/*JSONObject getSchoolOrgType(JSONObject obj);
	void insertSchoolOrgType(JSONObject obj);
	void deleteSchoolOrgType(JSONObject obj);*/
	JSONObject getSchoolOrg(JSONObject obj);
	void insertSchoolOrg(JSONObject obj);
	void deleteSchoolOrg(JSONObject obj);
	void deleteOrgLesson(JSONObject obj);
	void deleteOrgScope(JSONObject obj);
	void insertOrgLessonBatch(List<JSONObject> ls);
	void insertOrgScopeBatch(List<JSONObject> ls);
	List<JSONObject> getOrgLessonList(JSONObject obj);
	List<JSONObject> getOrgScopeList(JSONObject obj);
	List<JSONObject> getCurrentLevelGradeList(JSONObject obj);
	List<JSONObject> getTeacherByLessonAndGrade(JSONObject obj);
	List<JSONObject> getTeacherByLessonAndGradeBatch(JSONObject obj);
	List<JSONObject> getLessonList(JSONObject param);
	void insertStaffBatch(List<JSONObject> ls);
	void deleteStaff(JSONObject obj);
	int addImportDepartmentBatch(Map<String, Object> needInsert);
	List<JSONObject> getOrgObj(JSONObject obj);
	List<String> getLeaders(JSONObject leaderJson);
	void deleteOrgMemberByUserId(JSONObject obj);
	List<JSONObject> getTeacherListBySchoolId(JSONObject obj);
	void insertOrgBatch(List<JSONObject> orgList);
	void insertSchoolOrgBatch(List<JSONObject> orgList);
	List<String> getMembers(JSONObject obj);
	List<String> getStaffIdsList(JSONObject obj);
	List<String> getStaffIdsListNotInOrg(JSONObject obj);
	void deleteOrgLeaderByUserId(JSONObject param);
	List<JSONObject> getOrgScopeGradeList(JSONObject scopeObj);
}
