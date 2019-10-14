package com.talkweb.systemManager.service.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.MD5Util;
import com.talkweb.systemManager.dao.UserDao;
import com.talkweb.systemManager.domain.business.Role;
import com.talkweb.systemManager.domain.business.TTrSchool;
import com.talkweb.systemManager.domain.business.TUcUser;
import com.talkweb.systemManager.exception.DeleteOperationException;
import com.talkweb.systemManager.exception.InsertOperationException;
import com.talkweb.systemManager.service.UserService;
/**
 * @ClassName UserServiceImpl
 * @author Homer
 * @version 1.0
 * @Description 系统管理Service
 * @date 2015年3月3日
 */
@Service(value="userServiceImpl2")
public class UserServiceImpl implements UserService{
	
	@Autowired
	private UserDao userDaoImpl2;

	@Override
	public int selectUserId(String userId) {
		
		int count = userDaoImpl2.selectUserId(userId);	
		return count;
	}

	@Override
	public TUcUser selectUser(String userId, String passWord) {
		
		TUcUser  user = userDaoImpl2.selectUser(userId, passWord);
		return user;
	}

	@Override
	public TTrSchool selectSchoolByUserId(String userId) {
		
		TTrSchool school  = userDaoImpl2.selectSchoolByUserId(userId);
		return school;
	}

	@Override
	public int updatePassword(String oldPassWord,String passWord,String userId){
		
		int result = userDaoImpl2.updatePassword(oldPassWord, passWord, userId);
		return result;
	}

	@Override
	public int updateAccount(String newUserId, String passWord, String userId){
		
		int result = userDaoImpl2.updateAccount(newUserId, passWord, userId);
		return result;
	}

	@Override
	public List<Map<String, Object>> getMySysMenu(String userSysId,String sf) {
		return userDaoImpl2.getMySysMenu(userSysId,sf);
	}

	@Override
	public List<Map<String, Object>> getSubSys(String userSysId) {
		return userDaoImpl2.getSubSys(userSysId);
	}

	@Override
	public List<Map<String, Object>> getSortName(String userSysId) {
		return userDaoImpl2.getSortName(userSysId);
	}

	@Override
	public List<String> getStudentXHByParentSysId(String parentsysid) {
		return userDaoImpl2.getStudentXHByParentSysId(parentsysid);
	}

	@Override
	public List<String> getRoleByUserSysId(String userSysId) {
		return userDaoImpl2.getRoleByUserSysId(userSysId);
				
	}

	

	@Override
	public List<Map<String, Object>> querySetManagerList(String xxdm,
			String roleDm, String zghxm) {
		return userDaoImpl2.getSetManagerList(xxdm, roleDm, zghxm);
	}

//	@Override
//	public int addManagerList(String roleDm, List<String> zgh, List<String> grade) throws Exception {
//		
//		int result = 0;
//		
//		if(null == grade || grade.size() == 0){
//			//系统管理员
//			result = userDaoImpl2.insertManagerList2(roleDm, zgh);
//		}else{
//			result = userDaoImpl2.insertManagerList(roleDm, zgh.get(0), grade);
//		}
//		
//		
//		if(result == 0){
//			throw new Exception("权限设置失败");
//		}
//		
//		return result;
//		
//	}

	@Override
	public int deleteManagerList(String roleDm, String zgh) throws DeleteOperationException {
		
		int result = userDaoImpl2.deleteManagerList(roleDm, zgh);
		if(result < 1){
			throw new DeleteOperationException("删除失败！");
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> getManagerGradeList(String xxdm) {
		
		List<Map<String, Object>> grades = userDaoImpl2.getManagerGradeList(xxdm);
		return grades;
		
	}

    @Override
    public JSONArray getUserSysMenu(String userSysId) {
        JSONArray rs = new JSONArray();
        JSONArray r = userDaoImpl2.getUserSysMenu(userSysId);
        //暂时不做树
//        rs = getRightList(0,r,rs);
        
//        System.out.println("JSONRS:---"+rs.toJSONString());
//        }
        return r;
    }

    private JSONArray getRightList(int i, JSONArray r,JSONArray rs) {
        JSONObject cur = r.getJSONObject(i);
        // TODO Auto-generated method stub
        JSONArray children = new JSONArray();
        String treeDm = cur .getString("treeDm");
//        if(type.equalsIgnoreCase("add") ){
        rs.add(cur);
//        }
        for(int j=i+1;j<r.size();j++){
            JSONObject next = r.getJSONObject(j);
            String nextDm = next.getString("treeDm");
            if(nextDm.indexOf(treeDm)>-1){
                children.add(next);
            }else{
                if(children.size()>0){
                    children = getRightList(0,children,new JSONArray());
                    cur.put("children", children); 
                }
                rs = getRightList(j,r,rs);
                break;
            }
        }
        return rs;
    }	
		

	@Override
	public List<Map<String, Object>> getTeacherQXXX(List<String> zgh) {
		
		List<Map<String, Object>> teacherQXXX = userDaoImpl2.getTeacherQXXX(zgh);
		return teacherQXXX;
	}

	@Override
	public List<Map<String, Object>> queryTeacherAccountList(String xxdm,String zghxm) {
		
		List<Map<String, Object>> accounts = userDaoImpl2.getTeacherAccountList(xxdm,zghxm);
		return accounts;
	}

	@Override
	public int updatePwdsEqualsAccount(String zgh) {
		
		//系统代码获取账号-oriPassword
		String account = userDaoImpl2.getUserIDByUserSysId(zgh);
		//加密后的账号-passWord
		String md2Account = MD5Util.getMD5String(account);
		
		int result = userDaoImpl2.updatePwdsEqualsAccount(zgh, md2Account, account);
		
		return result;
		
	}

	@Override
	public int updatePwds(List<String> zgh, String passWord, String oriPassWord) {
		
		String md5Pass = MD5Util.getMD5String(passWord);
		int result = userDaoImpl2.updatePwds(zgh, md5Pass, oriPassWord);
		return result;
	}

	@Override
	public List<Map<String, Object>> queryStudentAccountList(String xxdm,
			String xn, String xqm, List<String> synj, List<String> bjmc,
			String xjhxm) {
		
		List<Map<String, Object>> accounts = userDaoImpl2.getStudentAccountList(xxdm, xn, xqm, synj, bjmc, xjhxm);
		return accounts;
		
		
	}

	@Override
	public List<Map<String, Object>> queryParentAccountList(String xxdm,
			String xn, String xqm, List<String> synj, List<String> bjmc,
			String xjhxm) {
		
		List<Map<String, Object>> accounts = userDaoImpl2.getParentAccountList(xxdm, xn, xqm, synj, bjmc, xjhxm);
		return accounts;
	}

    @Override
    public JSONArray getModelsInfo(HashMap map) {
        // TODO Auto-generated method stub
        
        JSONArray arr = userDaoImpl2.getModelsInfo(map);
        return arr;
    }

    //成绩管理员等角色
	@Override
	public List<Map<String,Object>> insertManagerList(String roleDm, String zgh, List<String> grade) throws InsertOperationException {
		
		
		int result = userDaoImpl2.insertManagerList(roleDm, zgh, grade);
		
		if(result == 0){
			throw new InsertOperationException("添加失败");
		}
			
		List<String> zghs = new ArrayList<String>();
		zghs.add(zgh);
		List<Map<String, Object>> teacherQXXX = userDaoImpl2.getTeacherQXXX2(zghs,roleDm);
		return teacherQXXX;
		
	}

	//系统管理员调用
	@Override
	public List<Map<String,Object>> insertManagerList2(String roleDm, List<String> zgh) throws InsertOperationException {
		
		
		int result = userDaoImpl2.insertManagerList2(roleDm, zgh);
		
		if(result==0){
			throw new InsertOperationException("添加失败");
		}
		
		List<Map<String, Object>> teacherQXXX = userDaoImpl2.getTeacherQXXX(zgh);
		return teacherQXXX;
	}

	@Override
	public List<Role> querySuperManagerList(String xxdm) {
		List<Role> roles = userDaoImpl2.getSuperManagerList(xxdm);
		return roles;
	}

	@Override
	public List<Role> queryOtherManagerList(String xxdm) {
		List<Role> roles = userDaoImpl2.getOtherManagerList(xxdm);
		return roles;
	}

	@Override
	public List<Map<String, Object>> querySetOtherManagerList(String xxdm,
			String roleDm, String zghxm) {
		
		return userDaoImpl2.getSetOtherManagerList(xxdm, roleDm, zghxm);
		
	}

	@Override
	public int deleteOtherManagerList(String roleDm, String zgh) throws DeleteOperationException{
		
		int result =  userDaoImpl2.deleteOtherManagerList(roleDm, zgh);
		if(result<1){
			throw new DeleteOperationException("删除失败");
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> getTeacherQXXX2(List<String> zgh,String roleDm) {
		List<Map<String, Object>> teacherQXXX = userDaoImpl2.getTeacherQXXX2(zgh,roleDm);
		return teacherQXXX;
	}
	
	
	

}
