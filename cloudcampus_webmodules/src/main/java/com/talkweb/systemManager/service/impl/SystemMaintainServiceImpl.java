package com.talkweb.systemManager.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.talkweb.common.tools.MD5Util;
import com.talkweb.systemManager.dao.SystemMaintainDao;
import com.talkweb.systemManager.dao.UserDao;
import com.talkweb.systemManager.exception.BusinessDataExistsException;
import com.talkweb.systemManager.exception.DeleteOperationException;
import com.talkweb.systemManager.exception.InsertOperationException;
import com.talkweb.systemManager.service.SystemMaintainService;
/**
 * @ClassName SystemMaintainServiceImpl
 * @author Homer
 * @version 1.0
 * @Description 学校维护业务层实现类
 * @date 2015年3月4日
 */
@Service
public class SystemMaintainServiceImpl implements SystemMaintainService {

	@Autowired
	private SystemMaintainDao systemMaintainDaoImpl;
	@Autowired
	private UserDao userDaoImpl2;
	
	@Override
	public List<Map<String, Object>> querySchoolList(String xzqhm, String xxmc) {
		
		List<Map<String, Object>> schoolList = null;	
		
		String zqm = xzqhm.substring(2);	
		String sqm = xzqhm.substring(4);	
		if(xzqhm.contains(",")){
		   //查询全部省市县
		   schoolList = systemMaintainDaoImpl.getSchoolList("allarea",xxmc);	
		}else{
			//省级单位
			if("0000".equals(zqm)){
				schoolList = systemMaintainDaoImpl.getSchoolList(xzqhm.substring(0, 2),xxmc);
			//市级单位
			}else if("00".equals(sqm)){
				schoolList = systemMaintainDaoImpl.getSchoolList(xzqhm.substring(0, 4),xxmc);
			//县级单位
			}else{
				schoolList = systemMaintainDaoImpl.getSchoolList(xzqhm,xxmc);
			}
		}
		
		return schoolList;
		
	}

	@Override
	public int addSchool(String xzqhm, String xxdm, String xxmc, String xxlb,String dmid,
			List<String> pycc, String userId, String password) throws InsertOperationException {
		
		int result = systemMaintainDaoImpl.insertSchool(xzqhm, xxdm, xxmc, xxlb,dmid);
		
		result += systemMaintainDaoImpl.insertSchoolPycc(xxdm, pycc);
		
		String md5Password = MD5Util.getMD5String(password);
		
		result += systemMaintainDaoImpl.insertSchoolUser(xxdm,userId,md5Password, password, "0", "001", xxdm);
		
		if(result < 3){
			throw new InsertOperationException("添加学校失败");
		}
		
		return result;
		
	}

	@Override
	public int deleteSchool(List<String> xxdm) throws BusinessDataExistsException, DeleteOperationException {
		
		List<String> xxdmList = new ArrayList<String>();
		int num = 0;
		for (Iterator<String> iterator = xxdm.iterator(); iterator.hasNext();) {
			String txxdm = (String) iterator.next();
			num += systemMaintainDaoImpl.getNumOfClassFromSchool(txxdm);
			num += systemMaintainDaoImpl.getNumOfStudentFromSchool(txxdm);
			num += systemMaintainDaoImpl.getNumOfTeacherFromSchool(txxdm);
			
			if(num > 0){
				xxdmList.add(txxdm);
			}
		}
		
		if(xxdmList.size() > 0){
			throw new BusinessDataExistsException("业务数据已经存在，不能删除");
		}
		
		int result = systemMaintainDaoImpl.deleteSchool(xxdm);
		result += systemMaintainDaoImpl.deleteSchoolPycc(xxdm);
		result += systemMaintainDaoImpl.deleteSchoolUser(xxdm);
				
		if(result < 3){
			throw new DeleteOperationException("删除学校失败");
		}
		
		return result;
	}

	@Override
	public List<Map<String, Object>> queryTeacherAccountList(String xzqhm,
			String xxdm, String zghxm) {
		//当前省份区划码
		String sxzqhm = xzqhm.substring(0, 2) + "0000";
		//当前市区划码
		String shxzqhm = xzqhm.substring(0, 4) + "00";
		
		List<Map<String, Object>> accounts = systemMaintainDaoImpl.getTeacherAccountList(xzqhm, sxzqhm, shxzqhm, xxdm, zghxm);
	
		return accounts;
	}

	@Override
	public int updatePwdsEqualsAccount(String zgh) {
		//系统代码获取账号-oriPassword
	    String account = userDaoImpl2.getUserIDByUserSysId(zgh);
		//加密后的账号-passWord
		String md2Account = MD5Util.getMD5String(account);
				
		int result = systemMaintainDaoImpl.updatePwdsEqualsAccount(zgh, md2Account, account);
				
		return result;
	}

	@Override
	public int updatePwds(List<String> zgh, String passWord, String oriPassWord) {
		String md5Pass = MD5Util.getMD5String(passWord);
		int result = systemMaintainDaoImpl.updatePwds(zgh, md5Pass, oriPassWord);
		return result;
	}

	@Override
	public List<Map<String, Object>> queryStudentAccountList(String xzqhm,
			String xxdm, String xn, String xqm, List<String> synj,
			List<String> bjmc, String xjhxm) {
	
		//当前省份区划码
		String sxzqhm = xzqhm.substring(0, 2) + "0000";
		//当前市区划码
		String shxzqhm = xzqhm.substring(0, 4) + "00";
				
		List<Map<String, Object>> accounts = systemMaintainDaoImpl.getStudentAccountList(xzqhm, sxzqhm, shxzqhm, xxdm, xn, xqm, synj, bjmc, xjhxm);
			
		return accounts;
			
		
	}

	@Override
	public List<Map<String, Object>> queryParentAccountList(String xzqhm,
			String xxdm, String xn, String xqm, List<String> synj,
			List<String> bjmc, String xjhxm) {
		//当前省份区划码
	    String sxzqhm = xzqhm.substring(0, 2) + "0000";
		//当前市区划码
		String shxzqhm = xzqhm.substring(0, 4) + "00";
		
		List<Map<String, Object>> accounts = systemMaintainDaoImpl.getParentAccountList(xzqhm, sxzqhm, shxzqhm, xxdm, xn, xqm, synj, bjmc, xjhxm);
		
		return accounts;
			
	}

	@Override
	public List<Map<String, Object>> querySchoolByAreaCode(String xzqhm) {
		List<Map<String, Object>> schoolList = null;	
		
		String zqm = xzqhm.substring(2);
		String sqm = xzqhm.substring(4);	
		
		//省级单位
		if("0000".equals(zqm)){
			schoolList = systemMaintainDaoImpl.getSchoolByAreaCode(xzqhm.substring(0, 2));
		//市级单位
		}else if("00".equals(sqm)){
			schoolList = systemMaintainDaoImpl.getSchoolByAreaCode(xzqhm.substring(0, 4));
		//县级单位
		}else{
			schoolList = systemMaintainDaoImpl.getSchoolByAreaCode(xzqhm);
		}
		
		return schoolList;
	}

	@Override
	public List<Map<String, Object>> getNJByXXXNXQ(String xxdm, String xn) {
		return systemMaintainDaoImpl.getNJByXXXNXQ(xxdm, xn);
	}

	@Override
	public List<Map<String, Object>> getClassByNJXXDM(String xxdm, String xn,
			String xqm, List<String> synj) {
		return systemMaintainDaoImpl.getClassByNJXXDM(xxdm, xn, xqm, synj);
	}

}
