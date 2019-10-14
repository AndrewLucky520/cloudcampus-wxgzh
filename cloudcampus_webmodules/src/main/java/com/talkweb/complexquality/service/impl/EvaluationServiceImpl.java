package com.talkweb.complexquality.service.impl;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.talkweb.complexquality.dao.EvaluationDao;
import com.talkweb.complexquality.service.EvalutionService;


@Service
public class EvaluationServiceImpl implements EvalutionService{
	
	@Autowired
	private EvaluationDao evaluationDao;

	@Override
	public List<Map<String, Object>> getStudentReative(Map<String, Object> param) {
		return evaluationDao.getStudentReative(param);
	}

	@Override
	public int addStudentReative(Map<String, Object> param) {
		return evaluationDao.addStudentReative(param);
	}

	@Override
	public int updateStudentReative(Map<String, Object> param) {
		return evaluationDao.updateStudentReative(param);
	}

}
