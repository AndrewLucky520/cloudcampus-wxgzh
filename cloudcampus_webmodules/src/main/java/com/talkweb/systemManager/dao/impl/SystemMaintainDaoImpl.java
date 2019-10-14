package com.talkweb.systemManager.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.systemManager.dao.SystemMaintainDao;
/**
 * @ClassName SystemMaintainDaoImpl
 * @author Homer
 * @version 1.0
 * @Description 学校维护数据访问层实现类
 * @date 2015年3月19日
 */
@Repository
public class SystemMaintainDaoImpl extends MyBatisBaseDaoImpl implements SystemMaintainDao {

	@Override
	public List<Map<String, Object>> getSchoolList(String xzqhm,String xxmc) {
		
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("xzqhm", xzqhm);
		params.put("xxmc", xxmc);
		
		List<Map<String,Object>> schoolList = selectList("getSchoolList", params);
		return schoolList;
	}

	@Override
	public int insertSchool(String xzqhm, String xxdm, String xxmc, String xxlb,String dmid) {
		
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("xzqhm", xzqhm);
		params.put("xxdm", xxdm);
		params.put("xxmc", xxmc);
		params.put("xxbxlxm", xxlb);
		params.put("dmid", dmid);
		int result = insert("insertSchool", params);
		return result;	
	}

	@Override
	public int insertSchoolPycc(String xxdm, List<String> pycc) {
		
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("xxdm", xxdm);
		params.put("pycc", pycc);
		int result = insert("insertSchoolPycc", params);
		return result;	
	}

	@Override
	public int insertSchoolUser(String xxdm,String userId,String password,
			String oriPassWord, String pwdStatus, String SF, String SSDW) {
		
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("xxdm", xxdm);
		params.put("userId", userId);
		params.put("password", password);
		params.put("oriPassWord", oriPassWord);
		params.put("pwdStatus", pwdStatus);
		params.put("sf", SF);
		params.put("ssdw", SSDW);	
		int result = insert("insertSchoolUser", params);
		return result;	
		
	}

	@Override
	public int deleteSchool(List<String> xxdm) {
		int result = delete("deleteSchool", xxdm);
		return result;
	}

	@Override
	public int deleteSchoolPycc(List<String> xxdm) {
		int result = delete("deleteSchoolPycc",xxdm);
		return result;
	}

	@Override
	public int deleteSchoolUser(List<String> xxdm) {
		int result = delete("deleteSchoolUser", xxdm);
		return result;
	}

	@Override
	public int updatePwdsEqualsAccount(String zgh, String passWord,
			String oriPassWord) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("zgh", zgh);
		params.put("passWord", passWord);
		params.put("oriPassWord", oriPassWord);
		
		int result = update("updatePwdsEqualsAccount", params);
		
		return result;
	}

	@Override
	public int updatePwds(List<String> zgh, String passWord, String oriPassWord) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("zgh", zgh);
		params.put("passWord", passWord);
		params.put("oriPassWord", oriPassWord);
		
		int result = update("updatePwds", params);
		
		return result;
	}

	@Override
	public List<Map<String, Object>> getTeacherAccountList(String xzqhm,String sxzqhm,String shxzqhm,
			String xxdm, String zghxm) {
			
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("xzqhm", xzqhm);
		params.put("shxzqhm",shxzqhm);
		params.put("sxzqhm", sxzqhm);
		params.put("xxdm", xxdm);
		params.put("zghxm",zghxm);
		List<Map<String, Object>> accounts = selectList("getBuTeacherAccountList",params);
		return accounts;
			
	}

		@Override
	    public List<Map<String,Object>> getStudentAccountList(String xzqhm,String sxzqhm,String shxzqhm,String xxdm,String xn,
	    		String xqm,List<String> synj,List<String> bjmc,String xjhxm){
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("xzqhm", xzqhm);
		params.put("sxzqhm", sxzqhm);
		params.put("shxzqhm", shxzqhm);
		params.put("xxdm", xxdm);
		params.put("xn", xn);
		params.put("xqm", xqm);
		params.put("synj", synj);
		params.put("bjmc", bjmc);
		params.put("xjhxm", xjhxm);
		
		List<Map<String, Object>> accounts = selectList("getBuStudentAccountList", params);
		
		return accounts;
		
	}

	@Override
	public List<Map<String,Object>> getParentAccountList(String xzqhm,String sxzqhm,String shxzqhm,String xxdm,String xn,
			String xqm,List<String> synj,List<String> bjmc,String xjhxm){
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("xzqhm", xzqhm);
		params.put("sxzqhm", sxzqhm);
		params.put("shxzqhm", shxzqhm);
		params.put("xxdm", xxdm);
		params.put("xn", xn);
		params.put("xqm", xqm);
		params.put("synj", synj);
		params.put("bjmc", bjmc);
		params.put("xjhxm", xjhxm);
		
		List<Map<String, Object>> accounts = selectList("getBuParentAccountList", params);
		
		return accounts;
	}

	@Override
	public int getNumOfTeacherFromSchool(String xxdm) {	
		int result = selectOne("getNumOfTeacherFromSchool", xxdm);
		return result;	
	}

	@Override
	public int getNumOfClassFromSchool(String xxdm) {
		int result = selectOne("getNumOfClassFromSchool", xxdm);
		return result;
	}

	@Override
	public int getNumOfStudentFromSchool(String xxdm) {
		int result = selectOne("getNumOfStudentFromSchool", xxdm);
		return result;
	}

	@Override
	public String getSchoolNameByCode(String xxdm) {		
		String xxmc = selectOne("getSchoolNameByCode", xxdm);
		return xxmc;
	}

	@Override
	public List<Map<String, Object>> getSchoolByAreaCode(String xzqhm) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("xzqhm", xzqhm);
		List<Map<String, Object>> schools = selectList("getSchoolByAreaCode", params);
		return schools;
	}

	@Override
	public List<Map<String, Object>> getNJByXXXNXQ(String xxdm, String xn) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("xxdm", xxdm);
		params.put("xn",xn);
		List<Map<String, Object>> synj = selectList("getNJByXXXNXQ", params);
		return synj;
	}

	@Override
	public List<Map<String, Object>> getClassByNJXXDM(String xxdm, String xn,
			String xqm, List<String> synj) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("xxdm", xxdm);
		params.put("xn",xn);
		params.put("xqm", xqm);
		params.put("synj", synj);
		List<Map<String, Object>> bj = selectList("getClassByNJXXDM", params);
		return bj;
	}

}
