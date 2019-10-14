package com.talkweb.commondata.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.commondata.dao.HisCommonDataDao;

/** 
 * @author  作者 E-mail: zhuxiaoyue@talkweb.com.cn
 * @date 创建时间：2015年12月23日 上午10:09:37 
 * @Description
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Repository
public class HisCommonDataDaoImpl  extends MyBatisBaseDaoImpl  implements HisCommonDataDao {

	@Override
	public List<JSONObject> getGradeListBySchoolId(JSONObject obj) { 
		return selectList("getGradeListBySchoolId",obj);
	}

	@Override
	public List<JSONObject> getClassListBy(HashMap<String, Object> map) {
		return selectList("getClassListBy",map);
	}

	@Override
	public List<JSONObject> getAccountIdsByUserId(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getAccountIdsBy",obj);
	}

	@Override
	public List<JSONObject> getStudentList(JSONObject params) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.commondata.dao.HisCommonDataDao.getStudentList", params);
	}

	@Override
	public List<JSONObject> getSchoolStudentList(JSONObject params) {
		// TODO Auto-generated method stub
		return selectList("getSchoolStudentList", params);
	}

	@Override
	public List<JSONObject> getLessonAndTeacherByClassId(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getLessonAndTeacherByClassId",obj);
	}

	@Override
	public List<JSONObject> getLessonListBySchoolId(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getLessonListBySchoolId",obj);
	}
	
	@Override
	public List<JSONObject> getSchoolOrgList(JSONObject param) {
		return selectList("getSchoolOrgList", param);
	}

	@Override
	public List<JSONObject> getOrgTeacherList(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getOrgTeacherList",obj);
	}
	
	@Override
	public List<JSONObject> getCourseTeacherList(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getCourseTeacherList",obj);
	}

	@Override
	public List<JSONObject> getDeanList(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getDeanList",obj);
	}

	@Override
	public List<JSONObject> getAllSchoolEmployees(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getAllSchoolEmployees",obj);
	}

	@Override
	public List<JSONObject> getOrgIntersectionTeacherList(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getOrgIntersectionTeacherList",obj);
	}

	@Override
	public List<JSONObject> getSchoolById(long schoolId,String termInfoId) {
		// TODO Auto-generated method stub
		JSONObject obj = new JSONObject();
		obj.put("termInfoId", termInfoId);
		obj.put("schoolId", schoolId);
		return selectList("getSchoolById",obj);
	}

	@Override
	public List<JSONObject> getAllClass(List<Long> gradeIds,String termInfoId) {
		// TODO Auto-generated method stub
		JSONObject obj = new JSONObject();
		obj.put("termInfoId",  termInfoId);
		obj.put("gradeIds", gradeIds);
		return selectList("getAllClass", obj);
	}
	@Override
	public List<JSONObject> getDeanIdByClassId(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getDeanIdByClassId",obj);
	}
	@Override
	public List<JSONObject> getAllStudent(JSONObject paramObj) {
		// TODO Auto-generated method stub
		return selectList("getAllStudent", paramObj);
	}

	@Override
	public JSONObject getBaseAccountUserIds(JSONObject params) {
		// TODO Auto-generated method stub
		return selectOne("getBaseAccountUserIds", params);
	}

	@Override
	public List<JSONObject> getUserIdsAndRole(long id,String termInfoId) {
		// TODO Auto-generated method stub
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("termInfoId", termInfoId);
		return selectList("getUserIdsAndRole", obj);
	}

	@Override
	public JSONObject getTeacherRole(Long id,String termInfoId) {
		// TODO Auto-generated method stub
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("termInfoId", termInfoId);
		return selectOne("getTeacherRole", obj);
	}

	@Override
	public JSONObject getStaffRole(Long id,String termInfoId) {
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("termInfoId", termInfoId);
		return selectOne("getStaffRole", obj);
	}

	@Override
	public JSONObject getSchoolManagerPartRole(Long id,String termInfoId) {
		// TODO Auto-generated method stub
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("termInfoId", termInfoId);
		return selectOne("getSchoolManagerPartRole", obj);
	}

	@Override
	public JSONObject getAccountById(JSONObject params) {
		// TODO Auto-generated method stub
		return selectOne("com.talkweb.commondata.dao.HisCommonDataDao.getAccountById", params);
	}
	@Override
	public List<JSONObject> getStudentIdByClassId(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getStudentIdByClassId",obj);
	}

	@Override
	public List<JSONObject> getParentIdByClassId(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getParentIdByClassId",obj);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年1月12日 上午11:32:29
	 *@version 版本
	 *@author liboqi
	 */
	public JSONObject getClassById(JSONObject jsonObject) {
		 return selectOne("getClassById",jsonObject);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年1月12日 上午11:32:29
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> getClassByStudent(JSONObject jsonObject) {
		 return selectList("getClassByStudent",jsonObject);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年1月12日 上午11:32:29
	 *@version 版本
	 *@author liboqi
	 */
	public JSONObject getUserById(JSONObject jsonObject) {
		 return selectOne("getUserById",jsonObject);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年1月12日 上午11:32:29
	 *@version 版本
	 *@author liboqi
	 */
	public JSONObject getStudentByUserId(JSONObject jsonObject) {
		 return selectOne("getStudentByUserId",jsonObject);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年1月12日 上午11:32:29
	 *@version 版本
	 *@author liboqi
	 */
	public List<Long> getOrgListByUserId(JSONObject jsonObject) {
		 return selectList("getOrgListByUserId",jsonObject);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年1月12日 上午11:32:29
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> getTeacherCourseListByUserId(JSONObject jsonObject) {
		 return selectList("getTeacherCourseListByUserId",jsonObject);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年1月12日 上午11:32:29
	 *@version 版本
	 *@author liboqi
	 */
	public JSONObject getAccountByUserId(JSONObject jsonObject) {
		 return selectOne("getAccountByUserId",jsonObject);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年1月12日 上午11:32:29
	 *@version 版本
	 *@author liboqi
	 */
	public JSONObject getParentByUserId(JSONObject jsonObject) {
		 return selectOne("getParentByUserId",jsonObject);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年1月12日 上午11:32:29
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> getAccountBatch(JSONObject jsonObject) {
		 return selectList("getAccountBatch",jsonObject);
	}

	/**
	 *@see 创建的原因
	 *@date 2016年1月28日 下午9:12:09
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryTeacherCourseByUserId(JSONObject jsonObject) {
		return selectList("queryTeacherCourseByUserId",jsonObject);
	}

	/**
	 *@see 创建的原因
	 *@date 2016年1月28日 下午9:12:09
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryAccountListByUserId(JSONObject jsonObject) {
		return selectList("queryAccountListByUserId",jsonObject);
	}

	/**
	 *@see 创建的原因
	 *@date 2016年1月28日 下午9:12:09
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryParentListByUserId(JSONObject jsonObject) {
		return selectList("queryParentListByUserId",jsonObject);
	}

	/**
	 *@see 创建的原因
	 *@date 2016年1月28日 下午9:23:00
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryTeacherRoleByUserId(JSONObject jsonObject) {
		return selectList("queryTeacherRoleByUserId",jsonObject);
	}

	/**
	 *@see 创建的原因
	 *@date 2016年1月28日 下午10:02:25
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryStudentListByUserId(JSONObject jsonObject) {
		return selectList("queryStudentListByUserId",jsonObject);
	}

	@Override
	public List<JSONObject> queryStaffListByUserId(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return selectList("queryStaffListByUserId",jsonObject);
	}

	@Override
	public List<JSONObject> queryManagerListByUserId(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return selectList("queryManagerListByUserId",jsonObject);
	}

	@Override
	public List<JSONObject> getDeanIdByClassIdNoAccount(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getDeanIdByClassIdNoAccount",obj);
	}

	@Override
	public List<JSONObject> getStudentIdByClassIdNoAccount(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getStudentIdByClassIdNoAccount",obj);
	}

	@Override
	public List<JSONObject> getParentIdByClassIdNoAccount(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getParentIdByClassIdNoAccount",obj);
	}

	@Override
	public List<JSONObject> getLessonAndTeacherByClassIdNoAccount(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("getLessonAndTeacherByClassIdNoAccount",obj);
	}

	@Override
	public List<JSONObject> queryStudentNameListByUserId(JSONObject jsonObject) {
		// TODO Auto-generated method stub
	   return selectList("queryStudentNameListByUserId",jsonObject);
	}

	@Override
	public List<JSONObject> getUserBatchById(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return selectList("getUserBatchById",jsonObject);
	}

	@Override
	public List<JSONObject> getTeacherDeanOfClassIds(Long userId, String termInfoId) {
		JSONObject json = new JSONObject();
		json.put("userId", userId);
		json.put("termInfoId", termInfoId);
		return selectList("getTeacherDeanOfClassIds",json);
	}

	@Override
	public List<Long> getSchoolTermIds(JSONObject json) {
		return selectList("getSchoolTermIds",json);
	}

	@Override
	public JSONObject getAreaCode(JSONObject param) {
		return selectOne("getAreaCode",param);
	}

	@Override
	public List<JSONObject> getAllOrgMembers(JSONObject param) {
		return selectList("getAllOrgMembers",param);
	}

	@Override
	public List<JSONObject> getTeacherHeaderAccount(JSONObject param) {
	return selectList("getTeacherHeaderAccount",param);
	}

	@Override
	public List<JSONObject> getAccountByIds(JSONObject json) {
		return selectList("getAccountByIds",json);
	}

	@Override
	public List<JSONObject> queryTeacherCourseList(JSONObject jsonObject2) {
		return selectList("queryTeacherCourseList",jsonObject2);
	}

	@Override
	public void updateClassroom(JSONObject basicClassObj) {
		update("updateClassroom",basicClassObj);
	}

	@Override
	public void deleteTeacherLesson(JSONObject param) {
		update("deleteTeacherLesson",param);
	}

	@Override
	public void insertTeacherLessonBatch(JSONObject p) {
		update("insertTeacherLessonBatch",p);
	}

	@Override
	public JSONObject getSchoolById1(Long schoolId, String termInfoId) {
		JSONObject json = new JSONObject();
		json.put("schoolId", schoolId);
		json.put("termInfoId", termInfoId);
		return selectOne("getSchoolById1",json);
	}
}
