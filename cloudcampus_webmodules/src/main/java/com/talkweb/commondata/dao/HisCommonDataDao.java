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
public interface HisCommonDataDao {

	List<JSONObject> getGradeListBySchoolId(JSONObject obj);
	List<JSONObject> getClassListBy(HashMap<String, Object> map);
	List<JSONObject> getDeanIdByClassId(JSONObject obj);
	List<JSONObject> getStudentIdByClassId(JSONObject obj);
	List<JSONObject> getParentIdByClassId(JSONObject obj);
	List<JSONObject> getDeanIdByClassIdNoAccount(JSONObject obj);
	List<JSONObject> getStudentIdByClassIdNoAccount(JSONObject obj);
	List<JSONObject> getParentIdByClassIdNoAccount(JSONObject obj);
	
	List<JSONObject> getAccountIdsByUserId(JSONObject obj);
	List<JSONObject> getLessonAndTeacherByClassId(JSONObject obj);
	List<JSONObject> getLessonAndTeacherByClassIdNoAccount(JSONObject obj);
	List<JSONObject> getLessonListBySchoolId(JSONObject obj);

	List<JSONObject> getOrgTeacherList(JSONObject obj);
	List<JSONObject> getOrgIntersectionTeacherList(JSONObject obj);
	List<JSONObject> getCourseTeacherList(JSONObject obj);
	List<JSONObject> getDeanList(JSONObject obj);
	List<JSONObject> getAllSchoolEmployees(JSONObject obj);
	
	List<JSONObject> getSchoolById(long schoolId,String termInfoId);
	
	List<JSONObject> getSchoolOrgList(JSONObject param);
	
	List<JSONObject> getStudentList(JSONObject params);
	List<JSONObject> getSchoolStudentList(JSONObject params);
	List<JSONObject> getAllClass(List<Long> gradeIds,String termInfoId);
	List<JSONObject> getAllStudent(JSONObject paramObj);
	JSONObject getBaseAccountUserIds(JSONObject params);
	List<JSONObject> getUserIdsAndRole(long id,String termInfoId);
	JSONObject getTeacherRole(Long id,String termInfoId);
	JSONObject getStaffRole(Long id,String termInfoId);
	JSONObject getSchoolManagerPartRole(Long id,String termInfoId);
	JSONObject getAccountById(JSONObject params);
	
	JSONObject getClassById(JSONObject jsonObject);
	List<JSONObject> getClassByStudent(JSONObject jsonObject);
	JSONObject getUserById(JSONObject jsonObject);
	JSONObject getStudentByUserId(JSONObject jsonObject);
	List<Long> getOrgListByUserId(JSONObject jsonObject);
	List<JSONObject> getTeacherCourseListByUserId(JSONObject jsonObject);
	JSONObject getAccountByUserId(JSONObject jsonObject);
	JSONObject getParentByUserId(JSONObject jsonObject);
	List<JSONObject> getAccountBatch(JSONObject jsonObject);
	List<JSONObject> queryTeacherCourseByUserId(JSONObject jsonObject);
	List<JSONObject> queryAccountListByUserId(JSONObject jsonObject);
	List<JSONObject> queryParentListByUserId(JSONObject jsonObject);
	List<JSONObject> queryTeacherRoleByUserId(JSONObject jsonObject);
	List<JSONObject> queryStudentListByUserId(JSONObject jsonObject);
	List<JSONObject> queryStudentNameListByUserId(JSONObject jsonObject);
	List<JSONObject> getUserBatchById(JSONObject jsonObject);
	List<JSONObject> queryStaffListByUserId(JSONObject jsonObject);
	List<JSONObject> queryManagerListByUserId(JSONObject jsonObject);
	List<JSONObject> getTeacherDeanOfClassIds(Long long1, String termInfoId);
	List<Long> getSchoolTermIds(JSONObject json);
	JSONObject getAreaCode(JSONObject param);
	List<JSONObject> getAllOrgMembers(JSONObject param);
	List<JSONObject> getTeacherHeaderAccount(JSONObject param);
	List<JSONObject> getAccountByIds(JSONObject json);
	List<JSONObject> queryTeacherCourseList(JSONObject jsonObject2);
	void updateClassroom(JSONObject basicClassObj);
	void deleteTeacherLesson(JSONObject param);
	void insertTeacherLessonBatch(JSONObject p);
	JSONObject getSchoolById1(Long schoolId, String termInfoId);
	
}
