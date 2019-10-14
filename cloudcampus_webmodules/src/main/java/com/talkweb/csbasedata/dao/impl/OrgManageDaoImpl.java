package com.talkweb.csbasedata.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.csbasedata.dao.OrgManageDao;
@Repository
public class OrgManageDaoImpl extends MyBatisBaseDaoImpl  implements OrgManageDao {

	@Override
	public List<JSONObject> getOrgList(JSONObject obj) {
		return selectList("getOrgListORG",obj);
	}

	@Override
	public int updateOrg(JSONObject obj) {
		return update("updateOrgORG",obj);
	}

	@Override
	public List<JSONObject> getOrgInfos(JSONObject obj) {
		return selectList("getOrgInfosORG",obj);
	}

	@Override
	public int deleteOrg(JSONObject obj) {
		return update("deleteOrgORG", obj);
	}

	@Override
	public int deleteOrgMember(JSONObject obj) {
		return update("deleteOrgMemberORG", obj);
	}

	@Override
	public int deleteOrgLeader(JSONObject obj) {
		return update("deleteOrgLeaderORG", obj);
	}

	@Override
	public List<JSONObject> getUserIdByAccountIdList(JSONObject obj) {
		return selectList("getUserIdByAccountIdListORG",obj);
	}

	@Override
	public void insertOrgMemberBatch(List<JSONObject> ms) {
		update("insertOrgMemberBatchORG",ms);
	}

	@Override
	public void insertOrgLeaderBatch(List<JSONObject> ls) {
		update("insertOrgLeaderBatchORG",ls);
	}

	@Override
	public void insertOrg(JSONObject obj) {
		update("insertOrgORG",obj);
	}

//	@Override
//	public JSONObject getSchoolOrgType(JSONObject obj) {
//		return selectOne("getSchoolOrgType",obj);
//	}
//
//	@Override
//	public void insertSchoolOrgType(JSONObject obj) {
//		update("insertSchoolOrgType",obj);
//	}
//
//	@Override
//	public void deleteSchoolOrgType(JSONObject obj) {
//		update("deleteSchoolOrgType",obj);
//		
//	}

	@Override
	public JSONObject getSchoolOrg(JSONObject obj) {
		return selectOne("getSchoolOrgORG",obj);
	}

	@Override
	public void insertSchoolOrg(JSONObject obj) {
		update("insertSchoolOrgORG",obj);
		
	}

	@Override
	public void deleteSchoolOrg(JSONObject obj) {
		update("deleteSchoolOrgORG",obj);
		
	}

	@Override
	public void deleteOrgLesson(JSONObject obj) {
		update("deleteOrgLessonORG",obj);
	}

	@Override
	public void deleteOrgScope(JSONObject obj) {
		update("deleteOrgScopeORG",obj);
	}

	@Override
	public void insertOrgLessonBatch(List<JSONObject> obj) {
		update("insertOrgLessonBatchORG",obj);
	}

	@Override
	public void insertOrgScopeBatch(List<JSONObject> ls) {
		update("insertOrgScopeBatchORG",ls);
	}


	@Override
	public List<JSONObject> getOrgLessonList(JSONObject obj) {
		return selectList("getOrgLessonListORG",obj);
	}

	@Override
	public List<JSONObject> getOrgScopeList(JSONObject obj) {
		return selectList("getOrgScopeListORG",obj);
	}

	@Override
	public List<JSONObject> getCurrentLevelGradeList(JSONObject obj) {
		return selectList("getCurrentLevelGradeListORG",obj);
	}

	@Override
	public List<JSONObject> getTeacherByLessonAndGrade(JSONObject obj) {
		return selectList("getTeacherByLessonAndGradeORG",obj);
	}

	@Override
	public List<JSONObject> getTeacherByLessonAndGradeBatch(JSONObject obj) {
		return selectList("getTeacherByLessonAndGradeBatchORG",obj);
	}

	@Override
	public List<JSONObject> getLessonList(JSONObject param) {
		return selectList("getLessonListORG",param);
	}

	@Override
	public void insertStaffBatch(List<JSONObject> ls) {
		update("insertStaffBatchORG",ls);
	}

	@Override
	public void deleteStaff(JSONObject obj) {
		update("deleteStaffORG",obj);
	}

	@Override
	public List<JSONObject> getOrgObj(JSONObject obj) {
		return selectList("getOrgObjORG",obj);
	}

	@Override
	public List<String> getLeaders(JSONObject leaderJson) {
		return selectList("getLeadersORG",leaderJson);
	}

	@Override
	public void deleteOrgMemberByUserId(JSONObject obj) {
		update("deleteOrgMemberByUserIdORG",obj);
	}

	@Override
	public List<JSONObject> getTeacherListBySchoolId(JSONObject obj) {
		return selectList("getTeacherListBySchoolIdORG",obj);
	}

	@Override
	public int addImportDepartmentBatch(Map<String, Object> needInsert) {
		int noNeedInsertNum= (int) needInsert.get("noNeedInsertNum");
		List<JSONObject> insertLeaderList = (List<JSONObject>) needInsert.get("insertLeaderList");
		List<JSONObject> insertMemberList = (List<JSONObject>) needInsert.get("insertMemberList");
		List<JSONObject> insertStaffList = (List<JSONObject>) needInsert.get("insertStaffList");
		List<String> deleteStaffList = (List<String>) needInsert.get("deleteStaffList");
		List<JSONObject> deleteLeaderList = (List<JSONObject>) needInsert.get("deleteLeaderList");
		List<JSONObject> deleteMemberList = (List<JSONObject>) needInsert.get("deleteMemberList");
		String schoolId = (String) needInsert.get("schoolId");
		int num =0;
		if(deleteLeaderList!=null && deleteLeaderList.size()>0){
			for(JSONObject delete:deleteLeaderList){
				update("deleteOrgLeaderListByUserIdORG",delete);
			}
		}
		if(deleteMemberList!=null && deleteMemberList.size()>0){
			for(JSONObject delete:deleteMemberList){
				update("deleteOrgMemberListByUserIdORG",delete);
			}
		}
		if(insertLeaderList!=null && insertLeaderList.size()>0){
			num = update("insertOrgLeaderBatchORG",insertLeaderList);
		}
		if(insertMemberList!=null && insertMemberList.size()>0){
			num+=update("insertOrgMemberBatchORG",insertMemberList);
		}
		if(insertStaffList!=null && insertStaffList.size()>0){
			update("insertStaffBatchORG",insertStaffList);
		}
		//判断待删除的staff是否不存在于任何的机构内，是则直接删除
		if(deleteStaffList!=null && deleteStaffList.size()>0){
			JSONObject staffObj = new JSONObject();
			staffObj.put("ids", deleteStaffList);
			//得到存在在机构中的staffIdList
			List<String> staffList = selectList("getOrgHeadAndStaffIdListORG",staffObj);
			//得到要删除的staff
			deleteStaffList.removeAll(staffList);
			JSONObject deleteStaffObj = new JSONObject();
			List<String> deleteStaffIdList = new ArrayList<String>();
			for(String dsId:deleteStaffList){
				deleteStaffIdList.add(dsId);
			}
			if(deleteStaffIdList.size()>0){
				deleteStaffObj.put("schoolId", schoolId);
				deleteStaffObj.put("ids", deleteStaffIdList);
				update("deleteStaffListByUserIdORG",deleteStaffObj);
			}
		}
		return num+noNeedInsertNum;
	}

	@Override
	public void insertOrgBatch(List<JSONObject> orgList) {
		update("insertOrgBatchORG",orgList);
	}

	@Override
	public void insertSchoolOrgBatch(List<JSONObject> orgList) {
		update("insertSchoolOrgBatchORG",orgList);
	}

	@Override
	public List<String> getMembers(JSONObject obj) {
		return selectList("getMembersORG",obj);
	}

	@Override
	public List<String> getStaffIdsList(JSONObject obj) {
		return selectList("getStaffIdsListORG",obj);
	}

	@Override
	public List<String> getStaffIdsListNotInOrg(JSONObject obj) {
		return selectList("getStaffIdsListNotInOrgORG",obj);
	}

	@Override
	public void deleteOrgLeaderByUserId(JSONObject param) {
		update("deleteOrgLeaderByUserIdORG",param);
	}

	@Override
	public List<JSONObject> getOrgScopeGradeList(JSONObject scopeObj) {
		return selectList("getOrgScopeGradeListORG",scopeObj);
	}

}
