package com.talkweb.student.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.talkweb.student.dao.QueryStudentStatisticsDao;
import com.talkweb.student.service.ClassStatic;

@Service
public class ClassStaticImpl implements ClassStatic {
	@Autowired
	private QueryStudentStatisticsDao queryStudentStatisticsDao;
	@Override
	public List<Map<String, Object>> getStatic(Map<String, Object> param) {
		return queryStudentStatisticsDao.QueryStudentInfo(param);
	}

}
