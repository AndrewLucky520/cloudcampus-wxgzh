package com.talkweb.systemManager.service;

import java.util.List;
import java.util.Map;

import com.talkweb.systemManager.exception.BusinessDataExistsException;
import com.talkweb.systemManager.exception.DeleteOperationException;
import com.talkweb.systemManager.exception.InsertOperationException;

/**
 * @ClassName SystemMaintainService
 * @author Homer
 * @version 1.0
 * @Description 学校维护业务层接口
 * @date 2015年3月19日
 */
public interface SystemMaintainService {

	List<Map<String,Object>> querySchoolList(String xzqhm,String xxmc);
	
	int addSchool(String xzqhm, String xxdm,String xxmc, String xxlb,String dmid,List<String> pycc,String userId,String password) throws InsertOperationException;
	
	int deleteSchool(List<String> xxdm) throws BusinessDataExistsException, DeleteOperationException;
	
	List<Map<String,Object>> queryTeacherAccountList(String xzqhm,String xxdm,String zghxm);
	
	
	List<Map<String,Object>> queryStudentAccountList(String xzqhm,String xxdm,String xn,String xqm,List<String> synj,List<String> bjmc,String xjhxm);
	
	
	List<Map<String,Object>> queryParentAccountList(String xzqhm,String xxdm,String xn,String xqm,List<String> synj,List<String> bjmc,String xjhxm);
	
	
	/**
	 * 修改密码为账号
	 * @param zgh 教师系统代码
	 * @return
	 */
	int updatePwdsEqualsAccount(String zgh);
	
	/**
	 * 修改密码为指定密码或者随机密码
	 * @param zgh 教师系统代码
	 * @param passWord 密码（秘文）
	 * @param oriPassWord 初始密码（明文）
	 * @return
	 */
	int updatePwds(List<String> zgh,String passWord,String oriPassWord);
	
	List<Map<String, Object>> querySchoolByAreaCode(String xzqhm);
	
	List<Map<String, Object>> getNJByXXXNXQ(String xxdm,String xn);
	
	List<Map<String,Object>> getClassByNJXXDM(String xxdm,String xn,String xqm,List<String> synj);
}
