package com.talkweb.teacher.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.talkweb.teacher.dao.TeacherClassDao;
import com.talkweb.teacher.service.TeacherClassService;

@Service
public class TeacherClassServiceImpl implements TeacherClassService {
	@Autowired
	private TeacherClassDao teacherClassDao;

	@Override
	public List<Map<String, Object>> getTeacherList(Map<String, Object> param) {
		return teacherClassDao.queryTeacherList(param);
	}

	@Override
	public List<Map<String, Object>> queryTeacherOne(Map<String, Object> param) {
		return teacherClassDao.queryTeacherOne(param);
	}

	@Override
	public boolean saveOneClassTeacher(Map<String, Object> param) {
		int rescode = teacherClassDao.saveOneClassTeacher(param);
		if (rescode > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<Map<String, Object>> querylessoninfo() {
		return teacherClassDao.querylessoninfo();

	}

	@Override
	public int saveDelete(Map<String, Object> del) {
		return teacherClassDao.saveDelete(del);
	}

}
