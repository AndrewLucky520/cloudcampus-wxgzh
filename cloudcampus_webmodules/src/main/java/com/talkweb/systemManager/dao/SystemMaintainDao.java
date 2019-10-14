package com.talkweb.systemManager.dao;

import java.util.List;
import java.util.Map;

/**
 * @ClassName SystemMaintainDao
 * @author Homer
 * @version 1.0
 * @Description 学校维护数据访问层接口
 * @date 2015年3月19日
 */
public interface SystemMaintainDao {

	List<Map<String,Object>> getSchoolList(String xzqhm,String xxmc);
	
	int insertSchool(String xzqhm,String xxdm,String xxmc,String xxlb,String dmid);
	
	int insertSchoolPycc(String xxdm,List<String> pycc);
	
	int insertSchoolUser(String xxdm,String userId,String password,String oriPassWord,String pwdStatus,String SF,String SSDW);
	
	int deleteSchool(List<String> xxdm);
	
	int deleteSchoolPycc(List<String> xxdm);
	
	int deleteSchoolUser(List<String> xxdm);
	
	List<Map<String,Object>> getTeacherAccountList(String xzqhm,String sxzqhm,String shxzqhm,String xxdm,String zghxm);
	
	
	List<Map<String,Object>> getStudentAccountList(String xzqhm,String sxzqhm,String shxzqhm,String xxdm,String xn,String xqm,List<String> synj,List<String> bjmc,String xjhxm);
	
	
	List<Map<String,Object>> getParentAccountList(String xzqhm,String sxzqhm,String shxzqhm,String xxdm,String xn,String xqm,List<String> synj,List<String> bjmc,String xjhxm);
	
	
	/**
	 * 修改密码为账号
	 * @param zgh 教师系统代码
	 * @return
	 */
	int updatePwdsEqualsAccount(String zgh,String passWord,String oriPassWord);
	
	
	/**
	 * 修改密码为指定密码或者随机密码
	 * @param zgh 教师系统代码
	 * @param passWord 密码（秘文）
	 * @param oriPassWord 初始密码（明文）
	 * @return
	 */
	int updatePwds(List<String> zgh,String passWord,String oriPassWord);
	
	
	int getNumOfTeacherFromSchool(String xxdm);
	
	int getNumOfClassFromSchool(String xxdm);
	
	int getNumOfStudentFromSchool(String xxdm);
	
	String getSchoolNameByCode(String xxdm);
	
	List<Map<String,Object>> getSchoolByAreaCode(String xzqhm);
	
	List<Map<String,Object>> getNJByXXXNXQ(String xxdm,String xn);
	
	List<Map<String,Object>> getClassByNJXXDM(String xxdm,String xn,String xqm,List<String> synj);
	
	
	
}
