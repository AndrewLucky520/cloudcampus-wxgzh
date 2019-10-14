package com.talkweb.salary.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.salary.dao.SalaryDao;
import com.talkweb.salary.domain.SalExcel;

@Repository
public class SalaryDaoImpl extends MyBatisBaseDaoImpl implements SalaryDao {

	@Override
	public List<JSONObject> getSalaryList(JSONObject object) {
		return selectList("getSalaryList", object);
	}

	@Override
	public int addSalary(JSONObject object) {
		return insert("addSalary", object);
	}

	@Override
	public int updateSalary(JSONObject object) {
		// TODO Auto-generated method stub
		return update("updateSalary", object);
	}

	@Override
	public int insertSalExcel(List<SalExcel> l_salExcel) {
		return insert("insertSalExcel", l_salExcel);
	}

	@Override
	public int insertSalDetail(List<JSONObject> l_params) {
		// TODO Auto-generated method stub
		return insert("insertSalDetail",l_params);
	}

	@Override
	public int updateSalaryPublished(JSONObject param) {
		// TODO Auto-generated method stub
		return update("updateSalaryPublished", param);
	}

	@Override
	public List<JSONObject> getSalaryId(JSONObject object) {
		// TODO Auto-generated method stub
		return selectList("getSalaryId", object);
	}

	@Override
	public List<JSONObject> getSalaryIdMax(JSONObject object) {
		// TODO Auto-generated method stub
		return selectList("getSalaryIdMax", object);
	}
	
	@Override
	public List<JSONObject> getSalaryDetail(JSONObject object) {
		// TODO Auto-generated method stub
		return selectList("getSalaryDetail", object);
	}

	@Override
	public List<JSONObject> getSalaryExcel(String salaryId) {
		// TODO Auto-generated method stub
		return selectList("getSalaryExcel", salaryId);
	}

	@Override
	public List<JSONObject> getAllSalaryExcel(JSONObject object) {
		// TODO Auto-generated method stub
		return selectList("getAllSalaryExcel",object);
	}

	@Override
	public List<JSONObject> getAllSalaryDetail(JSONObject object) {
		// TODO Auto-generated method stub
		return selectList("getAllSalaryDetail", object);
	}

	@Override
	public int updateSalaryImported(String salaryId) {
		// TODO Auto-generated method stub
		return update("updateSalaryImported",salaryId);
	}

	@Override
	public int deleteSalary(JSONObject object) {
		// TODO Auto-generated method stub
		return delete("deleteSalary", object);
	}

	@Override
	public String getSalaryNameById(String salaryId) {
		// TODO Auto-generated method stub
		return selectOne("getSalaryNameById", salaryId);
	}

	@Override
	public int deleteSalaryDetail(JSONObject object) {
		// TODO Auto-generated method stub
		return delete("deleteSalaryDetail", object);
	}

	@Override
	public int deleteSalaryExcel(JSONObject object) {
		// TODO Auto-generated method stub
		return delete("deleteSalaryExcel", object);
	}

	@Override
	public JSONObject getMaxYearMonth(JSONObject param) {
		// TODO Auto-generated method stub
		return selectOne("getMaxYearMonth",param);
	}

	@Override
	public JSONObject getCjSalaryAccount(JSONObject param) {
		 
		return selectOne("getCjSalaryAccount",param);
	}

	@Override
	public int updateCjSalaryAccount(JSONObject param) {
		return update("updateCjSalaryAccount",param);
	}

	@Override
	public int insertCjSalaryAccount(JSONObject param) {
		return update("insertCjSalaryAccount",param);
	}

	@Override
	public JSONObject getCjSchool(JSONObject param) {
		return selectOne("getCjSchool",param);
	}

	@Override
	public List<JSONObject> getTeacherBySalaryId(JSONObject param) {
		 
		return selectList("getTeacherBySalaryId" , param);
	}

}
