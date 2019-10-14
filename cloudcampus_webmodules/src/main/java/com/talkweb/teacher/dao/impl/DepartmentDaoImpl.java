package com.talkweb.teacher.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.teacher.dao.DepartmentDao;
import com.talkweb.teacher.domain.page.TTrBmxx;

/**
 * @version 2.0
 * @Description: 教师机构数据层实现类
 * @author 吴安辉
 * @date 2015年3月3日
 */

@Repository
public class DepartmentDaoImpl extends MyBatisBaseDaoImpl implements DepartmentDao {

	@Override
	public int addDepartment(TTrBmxx bmxx) {
		return insert("addDepartment",bmxx);
	}

	@Override
	public int deleteDepartment(String jgh){
		return delete("deleteDepartment",jgh);
	}

	@Override
	public int updateDepartment(TTrBmxx bmxx){
		return update("updateDepartment",bmxx);
	}

	@Override
	public List<Map<String,Object>> getDepartmentList(TTrBmxx bmxx){
		return selectList("getDepartmentList",bmxx);
	}
	
	@Override
	public List<Map<String,Object>> getGradeList(TTrBmxx bmxx){
		return selectList("getGradeList",bmxx);
	}

	@Override
	public TTrBmxx getOneDepartment(TTrBmxx bmxx){
		return selectOne("getOneDepartment",bmxx);
	}

	@Override
	public Map<String, Object> getOneGrade(TTrBmxx bmxx) {
		return selectOne("getOneGradeById",bmxx);
	}

	@Override
	public List<Map<String, Object>> getResearchGroupList(TTrBmxx bmxx) {
		return selectList("getResearchGroupList",bmxx);
	}

	@Override
	public List<Map<String, Object>> getOneResearchGroup(TTrBmxx bmxx) {
		return selectList("getOneResearchGroup",bmxx);
	}

	@Override
	public List<Map<String, Object>> getLessonPlanningGroupList(TTrBmxx bmxx) {
		return selectList("LessonPlanningGroupList",bmxx);
	}

	@Override
	public List<Map<String, Object>> getOneLessonPlanningGroup(TTrBmxx bmxx) {
		return selectList("getOneLessonPlanningGroup",bmxx);
	}

	@Override
	public List<Map<String, Object>> getLessonPlanningGroupYjkmList(
			List<String> bmxx) {
		return selectList("getLessonPlanningGroupYjkmList",bmxx);
	}

}
