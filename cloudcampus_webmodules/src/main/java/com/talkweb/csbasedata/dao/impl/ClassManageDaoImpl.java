package com.talkweb.csbasedata.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.csbasedata.dao.ClassManageDao;

@Repository
public class ClassManageDaoImpl extends MyBatisBaseDaoImpl implements
		ClassManageDao {
	
	@Override
	public JSONObject getCountByClassCond(JSONObject param) {
		return selectOne("getCountByClassCondition",param);
	}

	@Override
	public List<JSONObject> getClassList(JSONObject param) {
		return selectList("getClassList",param);
	}

	@Override
	public int addClass(JSONObject param) {
		return insert("insertClass",param);
	}

	@Override
	public int addClassList(List<JSONObject> list) {
		return insert("insertBaseDataClassList",list);
	}

	@Override
	public JSONObject getClassInfo(JSONObject param) {
		return selectOne("getClassInfo",param);
	}

	@Override
	public int updateClassInfo(JSONObject param) {
		return update("updateClassInfo",param);
	}

	@Override
	public int deleteClassCourses(List<Long> list) {
		return delete("deleteClassCourses",list);
	}

	@Override
	public int deleteClass(JSONObject param) {
		return delete("deleteBaseDataClass",param);
	}

	@Override
	public int addClassCourses(List<JSONObject> list) {
		return insert("insertClassCourses",list);
	}

	@Override
	public List<String> getStagesBySchoolId(String schoolId) {
		return selectList("getStagesBySchoolId",schoolId);
	}

	@Override
	public int deleteExtraGrade(JSONObject param) {
		return delete("deleteExtraGrade",param);
	}
	
	@Override
	public int deleteClassCourse(JSONObject param) {
		return delete("deleteClassCourse",param);
	}

	@Override
	public List<Long> getClassTeacherList(JSONObject param) {
		return selectList("getClassTeacherListCLASS",param);
	}

	@Override
	public void deleteClassCoursesBatchBy(JSONObject deletedClassCourses) {
		update("deleteClassCoursesBatchByCLASS",deletedClassCourses);
	}

	@Override
	public List<JSONObject> selectStudentByClassId(JSONObject json) {
		return selectList("selectStudentByClassIdCLASS",json);
	}

	@Override
	public void deleteStudentByClassId(JSONObject json) {
		update("deleteStudentByClassIdCLASS",json);
	}

	@Override
	public void deleteUser(JSONObject json) {
		update("deleteUserCLASS",json);
	}

	@Override
	public void deleteAccount(JSONObject json) {
		update("deleteAccountCLASS",json);
	}

	@Override
	public List<JSONObject> getParentListByStudentId(JSONObject json) {
		return selectList("getParentListByStudentIdCLASS",json);
	}

	@Override
	public void deleteUserExtend(JSONObject json) {
		update("deleteUserExtendCLASS",json);
		
	}
		
}