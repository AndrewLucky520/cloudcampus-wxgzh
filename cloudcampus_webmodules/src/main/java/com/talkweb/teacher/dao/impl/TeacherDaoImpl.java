package com.talkweb.teacher.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.teacher.dao.TeacherDao;
import com.talkweb.teacher.domain.page.THrTeacher;

@Repository
public class TeacherDaoImpl extends MyBatisBaseDaoImpl implements TeacherDao {

	@Override
	public int addTeacher(THrTeacher teacher) {
		return insert("addTeacher", teacher);
	}

	@Override
	public int deleteTeacher(String id) {
		return delete("delTeacher", id);
	}

	@Override
	public int updateTeacher(THrTeacher teacher) {
		return update("updateTeacher", teacher);
	}

	@Override
	public THrTeacher queryTeacherOne(String id) {
		return selectOne("selectById", id);
	}

	@Override
	public List<THrTeacher> queryTeacherList() {
		return selectList("selectAll");
	}

	@Override
	public List<THrTeacher> queryTeacherInLimit(Map<String, Object> limit) {
		return selectList("selectByLimit", limit);
	}

	@Override
	public int deleteTeacherByIdList(List<String> IdList) {
		return delete("deleteTeacherByIdMap", IdList);
	}

	@Override
	public String getZGHByUserSysId(String userSysId) {
		String zgh = selectOne("getZGHByUserSysId", userSysId);
		return zgh;
	}

    @Override
    public List<JSONObject> getAllTeaBySchoolNum(HashMap map) {
        // TODO Auto-generated method stub
        List<JSONObject> list = selectList("getTeacherListByXX",map);
        return list;
    }

    @Override
    public void insertTeaList(List<THrTeacher> needInsert) {
        // TODO Auto-generated method stub
        insert("insertTeaList", needInsert);
    }

}
