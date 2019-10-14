package com.talkweb.student.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.student.dao.StudentManageDao;
import com.talkweb.student.domain.business.TSsStudent;

/**
 * @version 2.0
 * @Description: 学生信息维护相关底层数据交互实现类
 * @author 吴安辉
 * @date 2015年3月3日
 */
@Repository
public class StudentManageDaoImpl extends MyBatisBaseDaoImpl implements StudentManageDao {

	@Override
	public int addStudent(TSsStudent student) {
		return insert("addStudentInfo",student);
	}

	@Override
	public int deleteStudent(List<String> student) {
		return delete("deleteStudent",student);
	}

	@Override
	public int deleteStudenrol(List<String> student) {
		return delete("deleteStudenrol",student);
	}

	@Override
	public int addStudenrol(Map<String,String> student) {
		return insert("addStudenrol",student);
	}

	@Override
	public List<JSONObject> getStudentList(Map<String, Object> param) {
		return selectList("com.talkweb.student.dao.StudentManageDao.getStudentListSTU",param);
	}

	@Override
	public TSsStudent getStudentById(String xh) {
		return selectOne("getStudentById",xh);
	}

	@Override
	public int updateStudent(TSsStudent student) {
		return update("updateStudent3",student);
	}

	@Override
	public int updateStudenrol(Map<String,String> student) {
		return update("updateStudenrol",student);
	}

	@Override
	public Map<String, Object> getXzInfoByBh(String bh) {
		return selectOne("getXzInfoByBh",bh);
	}

	@Override
	public Map<String, Object> getMaxXnxqByXh(String xh) {
		return selectOne("getMaxXnxqByXh",xh);
	}

	@Override
	public int deleteStudenrolByXh(Map<String, Object> param) {
		return delete("deleteStudenrolByXh",param);
	}


}
