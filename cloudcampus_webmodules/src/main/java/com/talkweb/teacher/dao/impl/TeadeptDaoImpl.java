package com.talkweb.teacher.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.teacher.dao.TeadeptDao;
import com.talkweb.teacher.domain.page.THrTeadept;

/**
 * @version 2.0
 * @Description: 教师机构人员设置相关底层数据交互实现类
 * @author 吴安辉
 * @date 2015年3月3日
 */
@Repository
public class TeadeptDaoImpl extends MyBatisBaseDaoImpl implements TeadeptDao {

	@Override
	public int addTeadept(THrTeadept teadept){
		return insert("addTeadept",teadept);
	}

	@Override
	public int deleteTeadept(THrTeadept teadept){
		return delete("deleteTeadept",teadept);
	}

	@Override
	public List<Map<String, Object>> getDepartmentStaff(Map<String,String> teadept){
		return selectList("getPeopleStaffList",teadept);
	}

	@Override
	public List<Map<String, Object>> getDepartmentNoStaff(
			Map<String, String> teadept) {
		return selectList("getPeopleNoStaffList",teadept);
	}

	@Override
	public List<Map<String, Object>> getLessonDepartmentStaff(Map<String,String> param) {
		return selectList("getLessonStaffList",param);
	}

}
