package com.talkweb.student.dao;

import java.util.List;
import java.util.Map;

public interface QueryStudentStatisticsDao {
	public List<Map<String, Object>> QueryStudentInfo(Map<String,Object> param);
}
