package com.talkweb.student.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.student.dao.ClassDao;
import com.talkweb.student.domain.business.TSsClass;

/**
 * @Version 2.0
 * @Description 班级信息维护
 * @author 雷智
 * @Data 2015-03-13
 */
@Repository
public class ClassDaoImpl extends MyBatisBaseDaoImpl implements ClassDao {

	@Override
	public List<Map<String,Object>> queryClassList(Map<String, Object> limit) {
		return selectList("queryClassList", limit);
	}

	@Override
	public int addClass(Map<String, Object> map) {
		return insert("addClass", map);
	}

	@Override
	public int addBatchClass(List<TSsClass> bClass) {
		return insert("addClassMany", bClass);
	}

	@Override
	public int updateClass(Map<String, Object> updatas) {
		return update("updateClass", updatas);
	}

	@Override
	public int delClass(List<String> dels) {
		return delete("deleteClass", dels)+delete("deleteClass2", dels);
	}

	@Override
	public Map<String,Object> queryClassOne(Map<String, Object> param) {
		return selectOne("queryOne", param);
	}

}
