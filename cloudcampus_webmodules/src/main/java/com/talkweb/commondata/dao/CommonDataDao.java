package com.talkweb.commondata.dao;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.commondata.domain.TEdTerminfo;

/**
 * @ClassName CommonDataDao
 * @author Homer
 * @version 1.0
 * @Description 公共数据Dao
 * @date 2015年3月3日
 */
@Repository
public class CommonDataDao extends MyBatisBaseDaoImpl {
	/**
	 * 获取入学年份,此方法作废，需接口调用
	 * 
	 * @return 入学年份
	 */
	public List<Map<String, Object>> getEntranceYear() {

		Map<String, Object> params = new Hashtable<String, Object>();
		int currentYear = DateUtil.getCalendar().get(Calendar.YEAR);
		params.put("currentYear", currentYear - 1);
		params.put("pv", "");
		List<Map<String, Object>> entranceYearList = selectList(
				"getEntranceYear", params);
		return entranceYearList;
	}

	/**
	 * 获得当前学年学期,此方法作废，需接口调用
	 * 
	 * @return 学年学期
	 */
	public TEdTerminfo getXNXQ() {

		TEdTerminfo xnxq = selectOne("getXNXQ");
		return xnxq;

	}
	

	/**
	 * 通过学年学期获得年级,此方法作废，需接口调用
	 * 
	 * @param xn
	 *            年级
	 * @return 年级
	 */
	public List<Map<String, Object>> getNJByXNXQ(int xn) {

		Map<String, Object> params = new Hashtable<String, Object>();
		params.put("xn", xn);
		List<Map<String, Object>> njList = selectList("getNJByXNXQ", params);
		return njList;

	}

	/**
	 * 通过年级获得班级，,此方法作废，需接口调用
	 * 
	 * @param xnxq
	 *            学年学期
	 * @param nj
	 *            年级
	 * @return 班级
	 */
	public List<Map<String, Object>> getBJByNJ(int xnxq, int nj) {

		Map<String, Object> params = new Hashtable<String, Object>();
		params.put("xnxq", xnxq);
		params.put("nj", nj);
		List<Map<String, Object>> bjList = selectList("getBJByNJ", params);
		return bjList;

	}

	/**
	 * 通过学年学期获得年级,此方法作废，需接口调用
	 * 
	 * @param xn
	 *            年级
	 * @return 年级
	 */
	public List<Map<String, Object>> getNJByXNXQ(HashMap<String, Object> params) {
		List<Map<String, Object>> njList = selectList("getNJByXNXQ", params);
		return njList;
	}


	/*	*//**
	 * 查询学校所有老师
	 * 
	 * @param xxdm
	 *            学校代码
	 * @return 老师列表
	 */
	/*
	 * public List<Map<String,Object>> getAllTeacherList(String xxdm){
	 * 
	 * List<Map<String,Object>> teacherList = selectList("getAllTeacherList",
	 * xxdm); return teacherList; }
	 */



	/*	*//**
	 * 查询学校所有学生
	 * 
	 * @param xxdm
	 *            学校代码
	 * @return 学生列表
	 */
	/*
	 * public List<JSONObject> getAllStudentList(String xxdm) {
	 * 
	 * List<JSONObject> studentList = selectList("getAllStudentList", xxdm);
	 * return studentList; }
	 */

	public List<Map<String, Object>> getSystemVersion() {

		List<Map<String, Object>> systemVersion = selectList("getSystemVersion");
		return systemVersion;

	}


	/**
	 * 综合科目列表
	 * 
	 * @return
	 */
	public List<JSONObject> getComprehensiveSubjects() {
		// TODO Auto-generated method stub
		return selectList("getComprehensiveSubjects");
	}

//	public List<JSONObject> getNoAuthorityClassList(HashMap map) {
//		// TODO Auto-generated method stub
//		return selectList("getNoAuthorityClassList", map);
//	}

	public List<JSONObject> getAllSchoolList(String termInfoId) {
		return selectList("getAllSchoolList",termInfoId);
	}
	public List<JSONObject> getSchoolInfoByName(String schoolName,String termInfoId) {
		JSONObject obj = new JSONObject();
		obj.put("schoolName",schoolName);
		obj.put("termInfoId",termInfoId);
		return selectList("getSchoolInfoByName",obj);
	}
	//@author :zhanghuihui  csCur长沙当前基础数据SQL
	public List<JSONObject> getTermInfos(JSONObject param){
		return selectList("getTermInfos",param);
	}

	public List<JSONObject> getTermInfoBatch(JSONObject param) {
		return selectList("getTermInfoBatch",param);
	}

	public List<JSONObject> getUserPermissionById(JSONObject param) {
		return selectList("getUserPermissionById",param);
	}

	public JSONObject getBaseAccountUserIdsByAccount(JSONObject params) {
		return selectOne("getBaseAccountUserIdsByAccount", params);
	}

	public JSONObject getTeacherRoleByAccount(Long id, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("id", id);
		param.put("termInfoId", termInfoId);
		return selectOne("getTeacherRoleByAccount",param);
	}

	
	public JSONObject getStaffById(long userId, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("userId", userId);
		param.put("termInfoId", termInfoId);
		return selectOne("getStaffById",param);
	}

	public JSONObject getParentById(long userId, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("userId", userId);
		param.put("termInfoId", termInfoId);
		return selectOne("getParentById",param);
	}

	public JSONObject getTeacherById(long userId, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("userId", userId);
		param.put("termInfoId", termInfoId);
		return selectOne("getTeacherById",param);
	}

	public JSONObject getStudentById(long userId, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("userId", userId);
		param.put("termInfoId", termInfoId);
		return selectOne("getStudentByIdCS",param);
	}
	
	public JSONObject getSchoolManagerById(long userId, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("userId", userId);
		param.put("termInfoId", termInfoId);
		return selectOne("getSchoolManagerById",param);
	}
	
  /*  public void deleteTeacherLesson(JSONObject param){
    	update("deleteTeacherLesson",param);
    }
    
    public void insertTeacherLessonBatch(JSONObject param){
    	update("insertTeacherLessonBatch",param);
    }

	public void updateClassroom(JSONObject basicClassObj) {
		update("com.talkweb.commondata.dao.CommonDataDao.updateClassroom",basicClassObj);
	}*/

	public List<JSONObject> getDicPermissions(JSONObject param) {
		return selectList("getDicPermissions",param);
	}

	public List<JSONObject> getOrgList(JSONObject obj) {
		return selectList("getOrgList",obj);
	}

	public List<JSONObject> getOrgScopeList(JSONObject obj) {
		return selectList("getOrgScopeList",obj);
	}

	public List<JSONObject> getTeacherByLessonOrGradeBatch(JSONObject obj) {
		return selectList("getTeacherByLessonOrGradeBatch",obj);
	}

	public List<JSONObject> getOrgInfos(JSONObject obj) {
		return selectList("getOrgInfos",obj);
	}

	public List<JSONObject> getOrgLessonList(JSONObject obj) {
		return selectList("getOrgLessonList",obj);
	}

	public List<JSONObject> getTeacherByLessonAndGradeBatch(JSONObject obj) {
		return selectList("getTeacherByLessonAndGradeBatch",obj);
	}

}
