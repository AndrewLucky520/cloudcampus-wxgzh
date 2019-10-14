package com.talkweb.student.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.student.dao.QueryStudentStatisticsDao;

@Repository
public class QueryStudentStatisticsDaoImpl extends MyBatisBaseDaoImpl implements
		QueryStudentStatisticsDao {
	@Override
	public List<Map<String, Object>> QueryStudentInfo(Map<String, Object> param) {
		return selectList("QueryStudentInfo", param);
	}

}
