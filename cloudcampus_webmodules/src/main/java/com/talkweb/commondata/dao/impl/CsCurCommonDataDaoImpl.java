package com.talkweb.commondata.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.commondata.dao.CsCurCommonDataDao;


@Repository
public class CsCurCommonDataDaoImpl  extends MyBatisBaseDaoImpl  implements CsCurCommonDataDao {

	public JSONObject getUserById(JSONObject jsonObject) {
		 return selectOne("getUserByIdCS",jsonObject);
	}
	

	@Override
	public List<JSONObject> getUserIdsAndRole(long id, String termInfoId) {
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("termInfoId", termInfoId);
		return selectList("getUserIdsAndRoleCS", obj);
	}

	@Override
	public JSONObject getStaffRole(Long id, String termInfoId) {
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("termInfoId", termInfoId);
		return selectOne("getStaffRoleCS", obj);
	}

	@Override
	public JSONObject getSchoolManagerPartRole(Long id, String termInfoId) {
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("termInfoId", termInfoId);
		return selectOne("getSchoolManagerPartRoleCS", obj);
	}

	@Override
	public List<JSONObject> getSchoolById(Long schoolId, String termInfoId) {
		JSONObject obj = new JSONObject();
		obj.put("termInfoId", termInfoId);
		obj.put("schoolId", schoolId);
		return selectList("getSchoolByIdCS",obj);
	}

	@Override
	public JSONObject getAreaCode(JSONObject param) {
		return selectOne("getAreaCodeCS",param);
	}

	@Override
	public List<JSONObject> getGradeListBySchoolId(JSONObject param) {
		return selectList("getGradeListBySchoolIdCS",param);
	}

	@Override
	public List<Long> getSchoolTermIds(JSONObject json) {
		return selectList("getSchoolTermIdsCS",json);
	}

	@Override
	public List<JSONObject> getAccountBatch(JSONObject jsonObject) {
		 return selectList("getAccountBatchCS",jsonObject);
	}

	@Override
	public List<JSONObject> queryStudentListByUserId(JSONObject jsonObject) {
		return selectList("queryStudentListByUserIdCS",jsonObject);
	}

	@Override
	public List<JSONObject> queryTeacherRoleByUserId(JSONObject jsonObject) {
		return selectList("queryTeacherRoleByUserIdCS",jsonObject);
	}

	@Override
	public List<JSONObject> queryStaffListByUserId(JSONObject jsonObject) {
		return selectList("queryStaffListByUserIdCS",jsonObject);
	}

	@Override
	public List<JSONObject> queryParentListByUserId(JSONObject jsonObject) {
		return selectList("queryParentListByUserIdCS",jsonObject);
	}

	@Override
	public List<JSONObject> queryStudentNameListByUserId(JSONObject jsonObject) {
		  return selectList("queryStudentNameListByUserIdCS",jsonObject);
	}

	@Override
	public List<JSONObject> queryManagerListByUserId(JSONObject jsonObject) {
		return selectList("queryManagerListByUserIdCS",jsonObject);
	}

	@Override
	public List<JSONObject> getCourseTeacherList(JSONObject obj) {
		return selectList("getCourseTeacherListCS",obj);
	}

	@Override
	public List<JSONObject> getDeanList(JSONObject obj) {
		return selectList("getDeanListCS",obj);
	}

	@Override
	public List<JSONObject> getAllSchoolEmployees(JSONObject obj) {
		return selectList("getAllSchoolEmployeesCS",obj);
	}

	@Override
	public List<JSONObject> getClassListBy(HashMap<String, Object> map) {
		return selectList("getClassListByCS",map);
	}

	@Override
	public List<JSONObject> getDeanIdByClassId(JSONObject classObj) {
		return selectList("getDeanIdByClassIdCS",classObj);
	}

	@Override
	public List<JSONObject> getStudentIdByClassId(JSONObject classObj) {
		return selectList("getStudentIdByClassIdCS",classObj);
	}

	@Override
	public List<JSONObject> getParentIdByClassId(JSONObject classObj) {
		return selectList("getParentIdByClassIdCS",classObj);
	}

	@Override
	public List<JSONObject> getLessonAndTeacherByClassId(JSONObject classObj) {
		return selectList("getLessonAndTeacherByClassIdCS",classObj);
	}

	@Override
	public List<JSONObject> getDeanIdByClassIdNoAccount(JSONObject classObj) {
		return selectList("getDeanIdByClassIdNoAccountCS",classObj);
	}

	@Override
	public List<JSONObject> getStudentIdByClassIdNoAccount(JSONObject classObj) {
		return selectList("getStudentIdByClassIdNoAccountCS",classObj);
	}

	@Override
	public List<JSONObject> getParentIdByClassIdNoAccount(JSONObject classObj) {
		return selectList("getParentIdByClassIdNoAccountCS",classObj);
	}

	@Override
	public List<JSONObject> getLessonAndTeacherByClassIdNoAccount(JSONObject classObj) {
		return selectList("getLessonAndTeacherByClassIdNoAccountCS",classObj);
	}

	@Override
	public List<JSONObject> getLessonListBySchoolId(JSONObject param) {
		return selectList("getLessonListBySchoolIdCS",param);
	}

	@Override
	public List<JSONObject> getSchoolStudentList(JSONObject params) {
		return selectList("getSchoolStudentListCS", params);
	}

	@Override
	public List<JSONObject> getStudentList(JSONObject params) {
		return selectList("getStudentListCS", params);
	}

	@Override
	public List<JSONObject> getSchoolOrgList(JSONObject param) {
		return selectList("getSchoolOrgListCS", param);
	}

	@Override
	public List<JSONObject> getAllOrgMembers(JSONObject param) {
		return selectList("getAllOrgMembersCS",param);
	}

	@Override
	public List<JSONObject> getUserBatchById(JSONObject jsonObject) {
		return selectList("getUserBatchByIdCS",jsonObject);
	}

	@Override
	public JSONObject getBaseAccountUserIds(JSONObject params) {
		return selectOne("getBaseAccountUserIdsCS", params);
	}

	@Override
	public List<JSONObject> getTeacherDeanOfClassIds(Long userId, String termInfoId) {
		JSONObject json = new JSONObject();
		json.put("userId", userId);
		json.put("termInfoId", termInfoId);
		return selectList("getTeacherDeanOfClassIdsCS",json);
	}

	@Override
	public JSONObject getAccountById(JSONObject params) {
		return selectOne("getAccountByIdCS", params);
	}

	@Override
	public List<JSONObject> getAllStudent(JSONObject paramObj) {
		return selectList("getAllStudentCS", paramObj);
	}

	@Override
	public JSONObject getTeacherRole(Long id, String termInfoId) {
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("termInfoId", termInfoId);
		return selectOne("getTeacherRoleCS", obj);
	}

	@Override
	public List<JSONObject> getClassByStudent(JSONObject jsonObject) {
		return selectList("getClassByStudentCS",jsonObject);
	}

	@Override
	public JSONObject getClassById(JSONObject jsonObject) {
		 return selectOne("getClassByIdCS",jsonObject);
	}

	@Override
	public List<JSONObject> getAllClass(List<Long> gids, String termInfoId) {
		JSONObject obj = new JSONObject();
		obj.put("termInfoId",  termInfoId);
		obj.put("gradeIds", gids);
		return selectList("getAllClassCS", obj);
	}

	@Override
	public JSONObject getParentByUserId(JSONObject jsonObject) {
		return selectOne("getParentByUserIdCS",jsonObject);
	}

	@Override
	public List<Long> getOrgListByUserId(JSONObject jsonObject) {
		return selectList("getOrgListByUserIdCS",jsonObject);
	}

	@Override
	public List<JSONObject> getTeacherCourseListByUserId(JSONObject jsonObject) {
		return selectList("getTeacherCourseListByUserIdCS",jsonObject);
	}

	@Override
	public JSONObject getStudentByUserId(JSONObject jsonObject) {
		 return selectOne("getStudentByUserIdCS",jsonObject);
	}

	@Override
	public JSONObject getAccountByUserId(JSONObject jsonObject) {
		 return selectOne("getAccountByUserIdCS",jsonObject);
	}


	@Override
	public List<JSONObject> getTermInfos(JSONObject param) {
		return selectList("getTermInfosCS",param);
	}


	@Override
	public List<JSONObject> getTermInfoBatch(JSONObject param) {
		return selectList("getTermInfoBatchCS",param);
	}


	@Override
	public JSONObject getParentById(long userId, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("userId", userId);
		param.put("termInfoId", termInfoId);
		return selectOne("getParentByIdCS",param);
	}


	@Override
	public JSONObject getStudentById(long userId, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("userId", userId);
		param.put("termInfoId", termInfoId);
		return selectOne("getStudentByIdCS",param);
	}


	@Override
	public JSONObject getTeacherById(long userId, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("userId", userId);
		param.put("termInfoId", termInfoId);
		return selectOne("getTeacherByIdCS",param);
	}


	@Override
	public JSONObject getStaffById(long userId, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("userId", userId);
		param.put("termInfoId", termInfoId);
		return selectOne("getStaffByIdCS",param);
	}


	@Override
	public JSONObject getSchoolManagerById(long userId, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("userId", userId);
		param.put("termInfoId", termInfoId);
		return selectOne("getSchoolManagerByIdCS",param);
	}


	@Override
	public List<JSONObject> getUserPermissionById(JSONObject param) {
		return selectList("getUserPermissionByIdCS",param);
	}


	@Override
	public List<JSONObject> getDicPermissions(JSONObject param) {
		return selectList("getDicPermissionsCS",param);
	}


	@Override
	public JSONObject getBaseAccountUserIdsByAccount(JSONObject params) {
		return selectOne("getBaseAccountUserIdsByAccountCS", params);
	}


	@Override
	public JSONObject getTeacherRoleByAccount(Long id, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("id", id);
		param.put("termInfoId", termInfoId);
		return selectOne("getTeacherRoleByAccountCS",param);
	}


	@Override
	public List<JSONObject> getAllSchoolList(String termInfoId) {
		return selectList("getAllSchoolListCS",termInfoId);
	}


	@Override
	public List<JSONObject> getSchoolInfoByName(String schoolName, String termInfoId) {
		JSONObject obj = new JSONObject();
		obj.put("schoolName",schoolName);
		obj.put("termInfoId",termInfoId);
		return selectList("getSchoolInfoByNameCS",obj);
	}


	@Override
	public void insertTeacherLessonBatch(JSONObject param) {
		update("insertTeacherLessonBatchCS",param);
		
	}


	@Override
	public void deleteTeacherLesson(JSONObject param) {
		update("deleteTeacherLessonCS",param);
	}


	@Override
	public void updateClassroom(JSONObject basicClassObj) {
		update("updateClassroomCS",basicClassObj);
	}


	@Override
	public List<JSONObject> getTeacherByLessonOrGradeBatch(JSONObject obj) {
		return selectList("getTeacherByLessonOrGradeBatchCS",obj);
	}


	@Override
	public List<JSONObject> getOrgInfos(JSONObject obj) {
		return selectList("getOrgInfosCS",obj);
	}


	@Override
	public List<JSONObject> getOrgScopeList(JSONObject obj) {
		return selectList("getOrgScopeListCS",obj);
	}


	@Override
	public List<JSONObject> getOrgLessonList(JSONObject obj) {
		return selectList("getOrgLessonListCS",obj);
	}


	@Override
	public List<JSONObject> getTeacherByLessonAndGradeBatch(JSONObject obj) {
		return selectList("getTeacherByLessonAndGradeBatchCS",obj);
	}
	
	@Override
	public List<JSONObject> getUserIdByExtId(JSONObject obj) {
		return selectList("getUserIdByExtIdCS",obj);
	}
	@Override
	public List<JSONObject> getUserIdByExtIdRole(JSONObject param) {
		return selectList("getUserIdByExtIdRoleCS",param);
	}

	@Override
	public List<JSONObject> getOrgIntersectionTeacherList(JSONObject obj) {
		return selectList("getOrgIntersectionTeacherListCS",obj);
	}


	@Override
	public List<JSONObject> getOrgTeacherList(JSONObject obj) {
		return selectList("getOrgTeacherListCS",obj);
	}


	@Override
	public List<JSONObject> getTeacherHeaderAccount(JSONObject param) {
		return selectList("getTeacherHeaderAccountCS",param);
	}


	@Override
	public List<JSONObject> getAccountByIds(JSONObject json) {
		return selectList("getAccountByIdsCS",json);
	}


	@Override
	public List<JSONObject> queryTeacherCourseList(JSONObject jsonObject2) {
	return selectList("queryTeacherCourseListCS",jsonObject2);
	}


	@Override
	public List<JSONObject> getAccountByRoleAndCondition(JSONObject param) {
		return selectList("getAccountByRoleAndConditionCS",param);
	}


	@Override
	public List<JSONObject> getUserIdByConditon(JSONObject param) {
		return selectList("getUserIdByConditonCS",param);
	}


	@Override
	public JSONObject getSchoolById1(Long schoolId, String termInfoId) {
		JSONObject json = new JSONObject();
		json.put("schoolId", schoolId);
		json.put("termInfoId", termInfoId);
		return selectOne("getSchoolById1CS",json); 
	}


	@Override
	public List<JSONObject> getExtAccountIdByUserIds(JSONObject relationshipParam) {
		return selectList("getExtAccountIdByUserIdsCS",relationshipParam);
	}


	@Override
	public List<JSONObject> getExtClassIdByclassId(JSONObject classParam) {
		return selectList("getExtClassIdByclassIdCS",classParam);
	}


	@Override
	public List<JSONObject> getUserIdsByExtId(JSONObject param) {
		return selectList("getUserIdsByExtIdCS",param);
	}


	@Override
	public List<JSONObject> getClassIdByExtclassId(JSONObject param) {
		return selectList("getClassIdByExtclassIdCS",param);
	}


	@Override
	public String getSchoolIdByExtId(JSONObject json) {
		return selectOne("getSchoolIdByExtIdCS",json);
	}


	@Override
	public List<Long> getModuleManagers(JSONObject param) {
		return selectList("getModuleManagersCS",param);
	}


	@Override
	public List<JSONObject> getExtLessonIdBySchoolId(JSONObject param) {
		return selectList("getExtLessonIdBySchoolIdCS",param);
	}


	@Override
	public List<JSONObject> getExtLessonIdByIds(JSONObject param) {
		return selectList("getExtLessonIdByIdsCS",param);
	}


	@Override
	public List<JSONObject> getStudentParentByClassId(JSONObject param) {
		return selectList("getClassByStudentCS", param);
	}


	@Override
	public List<JSONObject> getUserByAccountIds(JSONObject param) {
		return selectList("com.talkweb.commondata.dao.CsCurCommonDataDao.getUserByAccountIds", param);
	}


	@Override
	public List<JSONObject> getParentByStudentIds(JSONObject param) {
		return selectList("com.talkweb.commondata.dao.CsCurCommonDataDao.getParentByStudentIds", param);
	}
	@Override
	public List<JSONObject> getParentIdsByStu(JSONObject obj) {
		return selectList("getParentIdsByStuCS",obj);
	}


	@Override
	public List<JSONObject> getSimpleParentObj(JSONObject obj) {
		return selectList("getSimpleParentObjCS",obj);
	}
}
