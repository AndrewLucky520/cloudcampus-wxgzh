package com.talkweb.student.service;

import java.util.List;
import java.util.Map;

import com.talkweb.student.domain.business.TSsClass;

/**
 * @Version 2.0
 * @Description 班级信息维护
 * @author 雷智
 * @Data 2015-03-13
 */
public interface ClassService {
	public List<Map<String, Object>> queryClassList(Map<String, Object> limit);

	public Map<String, Object> queryClassOne(Map<String, Object> param);

	public Map<String, Object> addClass(Map<String, Object> map);

	public Map<String, Object> addBatchClass(List<TSsClass> bClass);

	public Map<String, Object> updateClass(Map<String, Object> updatas);

	public Map<String, Object> delClass(List<String> dels);
}
