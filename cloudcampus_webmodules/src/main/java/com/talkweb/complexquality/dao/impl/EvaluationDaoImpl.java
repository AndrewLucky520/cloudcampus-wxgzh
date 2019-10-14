package com.talkweb.complexquality.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.complexquality.dao.EvaluationDao;
@Repository
public class EvaluationDaoImpl extends MyBatisBaseDaoImpl implements EvaluationDao{

	@Override
	public List<Map<String, Object>> getStudentReative(Map<String, Object> param) {
		return selectList("getStudentReative",param);
	}

	@Override
	public int addStudentReative(Map<String, Object> param) {
		return insert("addStudentReative", param);
	}

	@Override
	public int updateStudentReative(Map<String, Object> param) {
		return update("updateStudentReative", param);
	}

}
