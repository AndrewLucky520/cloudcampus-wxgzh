package com.talkweb.commondata.dao;

import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

/** 
 * @author  作者 E-mail: zhuxiaoyue@talkweb.com.cn
 * @date 创建时间：2015年12月23日 上午10:08:33 
 * @Description
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
public interface CsCurCommonDataDao {

	JSONObject getUserById(JSONObject param);

	List<JSONObject> getUserIdsAndRole(long longValue, String termInfoId);

	JSONObject getStaffRole(Long long1, String termInfoId);

	JSONObject getSchoolManagerPartRole(Long long1, String termInfoId);

	List<JSONObject> getSchoolById(Long schoolId, String termInfoId);

	JSONObject getAreaCode(JSONObject param);

	List<JSONObject> getGradeListBySchoolId(JSONObject param);

	List<Long> getSchoolTermIds(JSONObject json);

	List<JSONObject> getAccountBatch(JSONObject jsonObject);

	List<JSONObject> queryStudentListByUserId(JSONObject jsonObject2);

	List<JSONObject> queryTeacherRoleByUserId(JSONObject jsonObject2);

	List<JSONObject> queryStaffListByUserId(JSONObject jsonObject2);

	List<JSONObject> queryParentListByUserId(JSONObject jsonObject2);

	List<JSONObject> queryStudentNameListByUserId(JSONObject jsonObject22);

	List<JSONObject> queryManagerListByUserId(JSONObject jsonObject2);

	List<JSONObject> getCourseTeacherList(JSONObject obj);

	List<JSONObject> getDeanList(JSONObject obj);

	List<JSONObject> getAllSchoolEmployees(JSONObject obj);

	List<JSONObject> getClassListBy(HashMap<String, Object> map);

	List<JSONObject> getDeanIdByClassId(JSONObject classObj);

	List<JSONObject> getStudentIdByClassId(JSONObject classObj);

	List<JSONObject> getParentIdByClassId(JSONObject classObj);

	List<JSONObject> getLessonAndTeacherByClassId(JSONObject classObj);

	List<JSONObject> getDeanIdByClassIdNoAccount(JSONObject classObj);

	List<JSONObject> getStudentIdByClassIdNoAccount(JSONObject classObj);

	List<JSONObject> getParentIdByClassIdNoAccount(JSONObject classObj);

	List<JSONObject> getLessonAndTeacherByClassIdNoAccount(JSONObject classObj);

	List<JSONObject> getLessonListBySchoolId(JSONObject param);

	List<JSONObject> getSchoolStudentList(JSONObject params);

	List<JSONObject> getStudentList(JSONObject params);

	List<JSONObject> getSchoolOrgList(JSONObject param);

	List<JSONObject> getAllOrgMembers(JSONObject param);

	List<JSONObject> getUserBatchById(JSONObject jsonObject);

	JSONObject getBaseAccountUserIds(JSONObject params);

	List<JSONObject> getTeacherDeanOfClassIds(Long long1, String termInfoId);

	JSONObject getAccountById(JSONObject params);

	List<JSONObject> getAllStudent(JSONObject paramObj);

	JSONObject getTeacherRole(Long long1, String termInfoId);

	List<JSONObject> getClassByStudent(JSONObject jsonObject);

	JSONObject getClassById(JSONObject jsonObject);

	List<JSONObject> getAllClass(List<Long> gids, String termInfoId);

	JSONObject getParentByUserId(JSONObject jsonObject);

	List<Long> getOrgListByUserId(JSONObject jsonObject);

	List<JSONObject> getTeacherCourseListByUserId(JSONObject jsonObject);

	JSONObject getStudentByUserId(JSONObject jsonObject);

	JSONObject getAccountByUserId(JSONObject jsonObject);

	List<JSONObject> getTermInfos(JSONObject param);

	List<JSONObject> getTermInfoBatch(JSONObject param);

	JSONObject getParentById(long userId, String termInfoId);

	JSONObject getStudentById(long userId, String termInfoId);

	JSONObject getTeacherById(long userId, String termInfoId);

	JSONObject getStaffById(long userId, String termInfoId);

	JSONObject getSchoolManagerById(long userId, String termInfoId);

	List<JSONObject> getUserPermissionById(JSONObject param);

	List<JSONObject> getDicPermissions(JSONObject param);

	JSONObject getBaseAccountUserIdsByAccount(JSONObject params);

	JSONObject getTeacherRoleByAccount(Long long1, String termInfoId);

	List<JSONObject> getAllSchoolList(String termInfoId);

	List<JSONObject> getSchoolInfoByName(String schoolName, String termInfoId);

	void insertTeacherLessonBatch(JSONObject p);

	void deleteTeacherLesson(JSONObject param);

	void updateClassroom(JSONObject basicClassObj);

	List<JSONObject> getTeacherByLessonOrGradeBatch(JSONObject obj);

	List<JSONObject> getOrgInfos(JSONObject obj);

	List<JSONObject> getOrgScopeList(JSONObject obj);

	List<JSONObject> getOrgLessonList(JSONObject obj);

	List<JSONObject> getTeacherByLessonAndGradeBatch(JSONObject obj);

	List<JSONObject> getUserIdByExtId(JSONObject obj);

	List<JSONObject> getOrgIntersectionTeacherList(JSONObject obj);

	List<JSONObject> getOrgTeacherList(JSONObject obj);

	List<JSONObject> getTeacherHeaderAccount(JSONObject param);

	List<JSONObject> getAccountByIds(JSONObject json);

	List<JSONObject> queryTeacherCourseList(JSONObject jsonObject2);

	List<JSONObject> getAccountByRoleAndCondition(JSONObject param);

	List<JSONObject> getUserIdByConditon(JSONObject param);

	JSONObject getSchoolById1(Long schoolId, String termInfoId);

	List<JSONObject> getExtAccountIdByUserIds(JSONObject relationshipParam);

	List<JSONObject> getExtClassIdByclassId(JSONObject classParam);

	List<JSONObject> getUserIdsByExtId(JSONObject param);

	List<JSONObject> getClassIdByExtclassId(JSONObject param);

	String getSchoolIdByExtId(JSONObject json);

	List<JSONObject> getUserIdByExtIdRole(JSONObject param);

	List<Long> getModuleManagers(JSONObject param);

	List<JSONObject> getExtLessonIdBySchoolId(JSONObject param);

	List<JSONObject> getExtLessonIdByIds(JSONObject param);
	
	List<JSONObject> getStudentParentByClassId(JSONObject param);
	
	List<JSONObject> getUserByAccountIds(JSONObject param);
	
	List<JSONObject> getParentByStudentIds(JSONObject param);

	List<JSONObject> getParentIdsByStu(JSONObject obj);

	List<JSONObject> getSimpleParentObj(JSONObject obj);
}