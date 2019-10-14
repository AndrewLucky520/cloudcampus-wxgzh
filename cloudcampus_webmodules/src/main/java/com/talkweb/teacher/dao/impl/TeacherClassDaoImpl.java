package com.talkweb.teacher.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.teacher.dao.TeacherClassDao;

@Repository
public class TeacherClassDaoImpl extends MyBatisBaseDaoImpl implements
		TeacherClassDao {
	@Override
	public List<Map<String, Object>> queryTeacherList(Map<String, Object> param) {
		return selectList("queryTeacherList", param);
	}

	@Override
	public List<Map<String, Object>> queryTeacherOne(Map<String, Object> param) {
		return selectList("queryTeacherOne", param);
	}

	@Override
	public int saveOneClassTeacher(Map<String, Object> param) {
		return update("saveOneTeacher", param);
	}

	@Override
	public List<Map<String, Object>> querylessoninfo() {
		return selectList("querylessoninfo");
	}

	@Override
	public int saveDelete(Map<String, Object> del) {
		return delete("saveDelete", del);
	}
}
