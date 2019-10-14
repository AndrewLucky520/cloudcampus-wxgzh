package com.talkweb.systemManager.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONArray;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.systemManager.dao.UserDao;
import com.talkweb.systemManager.domain.business.Role;
import com.talkweb.systemManager.domain.business.TTrSchool;
import com.talkweb.systemManager.domain.business.TUcUser;
/**
 * @ClassName UserDaoImpl
 * @author Homer
 * @version 1.0
 * @Description 系统管理Dao
 * @date 2015年3月4日
 */
@Repository(value="userDaoImpl2")
public class UserDaoImpl extends MyBatisBaseDaoImpl implements UserDao{
	
	@Override
	public int selectUserId(String userId) {
		
		int count = selectOne("selectUserId",userId);
		return count;
	}
	
	@Override
	public TUcUser selectUser(String userId, String passWord) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("userId", userId);
		params.put("passWord", passWord);
		TUcUser selectOne = selectOne("selectUser", params);
		return selectOne;
	}

	@Override
	public TTrSchool selectSchoolByUserId(String userId)  {
		
		TTrSchool school = selectOne("selectSchoolByUserId", userId);
		return school;
	}

	@Override
	public int updatePassword(String oldPassWord, String passWord,String userId) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("userId", userId);
		params.put("passWord", passWord);
		params.put("oldPassWord", oldPassWord);
		int result = update("updatePassword", params);
		return result;
	}

	@Override
	public int updateAccount(String newUserId, String passWord, String userId) {
		
		Map<String,String> params = new HashMap<String,String>();
		params.put("userSysId", userId);
		params.put("passWord", passWord);
		params.put("newUserId", newUserId);
		int result = update("updateAccount",params);
		return result;
	}

	@Override
	public List<Map<String, Object>> getMySysMenu(String userSysId,String sf) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("userSysId", userSysId);
		params.put("sf", sf);
		List<Map<String, Object>> sysMenus = selectList("getMySysMenu", params);
		return sysMenus;
	}

	@Override
	public List<Map<String, Object>> getSubSys(String userSysId) {
		
		List<Map<String, Object>> sysRegis = selectList("getSubSys", userSysId);
		return sysRegis;
	}

	@Override
	public List<Map<String, Object>> getSortName(String userSysId) {
		
		List<Map<String,Object>> sortNames = selectList("getSortName", userSysId);
		return sortNames;
	}

	@Override
	public List<String> getStudentXHByParentSysId(String parentsysid) {
		List<String> studentId = selectList("getStudentXHByParentSysId", parentsysid);
		return studentId;
	}

	@Override
	public List<String> getRoleByUserSysId(String userSysId) {
		List<Map<String,String>> roles = selectList("getRoleByUserSysId", userSysId);
		List<String> rs = new ArrayList<String>();
		for( Map<String, String> role :roles){
		    rs.add(role.get("rolecode"));
		}
		return rs;
	}

	@Override
	public List<Role> getSuperManagerList(String xxdm) {
		
		List<Role> roles = selectList("querySuperManagerList", xxdm);
		return roles;
	}
	
	
	@Override
	public List<Role> getOtherManagerList(String xxdm) {
		List<Role> roles = selectList("queryOtherManagerList", xxdm);
		return roles;
	}


	@Override
	public List<Map<String, Object>> getSetManagerList(String xxdm,
			String roleDm, String zghxm) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("roleDm", roleDm);
		params.put("xxdm",xxdm);
		params.put("zghxm", zghxm);
		
		List<Map<String, Object>> manages = selectList("querySetManagerList", params);
		return manages;
			
	}

	@Override
	public int insertManagerList(String roleDm,String zgh, List<String> grade) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("roleDm", roleDm);
		params.put("zgh",zgh);
		params.put("grade", grade);
		int result = insert("addManagerList", params);
		return result;
		
	}

    @Override
    public JSONArray getUserSysMenu(String userSysId) {
        
//        List<JSONObject> list = selectList("getUserMenuById",userSysId);
//        JSONArray rs = new JSONArray();
//        for(JSONObject obj : list){
//            rs.add(obj);
//        }
        List<Object> list = selectList("getUserMenuById",userSysId);
        JSONArray rs = new JSONArray(list);
        return rs;
    }

    @Override
    public JSONArray getModelsInfo(HashMap map) {
        // TODO Auto-generated method stub
        String srid =  (String) map.get("srid");
        List<Object> arr = new JSONArray();
        boolean isC = (boolean) map.get("isC");
        if(isC){
            arr =  selectList("getModelsInfoC",map);
        }else{
            if(srid!=null&&srid.trim().length()>0){
                arr =  selectList("getModelsInfoA",map);
            }else{
                arr =  selectList("getModelsInfoB",map);
            }
        }
        JSONArray rs = new JSONArray(arr);
        return rs;
    }

	
	@Override
	public int insertManagerList2(String roleDm, List<String> zgh) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("roleDm", roleDm);
		params.put("zgh",zgh);
		int result = insert("addManagerList2", params);
		return result;	
		
	}


	@Override
	public List<Map<String, Object>> getTeacherQXXX(List<String> zgh) {
		
		List<Map<String, Object>> qxxx = selectList("getTeacherQXXX", zgh);
		
		return qxxx;
	
	}

	@Override
	public int deleteManagerList(String roleDm, String zgh) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("roleDm", roleDm);
		params.put("zgh",zgh);
		int result = delete("delManagerList", params);
		return result;
	}

	@Override
	public List<Map<String, Object>> getManagerGradeList(String xxdm) {
	
		List<Map<String, Object>> grades = selectList("queryManagerGradeList", xxdm);
		return grades;
		
	}

	@Override
	public List<Map<String, Object>> getTeacherAccountList(String xxdm,String zghxm) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("xxdm", xxdm);
		params.put("zghxm",zghxm);
		List<Map<String, Object>> accounts = selectList("queryTeacherAccountList",params);
		return accounts;
		
	}

	@Override
	public String getUserIDByUserSysId(String zgh) {
		
		String userID = selectOne("getUserIDByUserSysId", zgh);
		return userID;
	}

	@Override
	public int updatePwdsEqualsAccount(String zgh,String passWord,String oriPassWord) {
		
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
	public List<Map<String, Object>> getStudentAccountList(String xxdm,
			String xn, String xqm, List<String> synj, List<String> bjmc, String xjhxm) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("xxdm", xxdm);
		params.put("xn", xn);
		params.put("xqm", xqm);
		params.put("synj", synj);
		params.put("bjmc", bjmc);
		params.put("xjhxm", xjhxm);
		
		List<Map<String, Object>> accounts = selectList("queryStudentAccountList", params);
		
		return accounts;
	}

	@Override
	public List<Map<String, Object>> getParentAccountList(String xxdm,
			String xn, String xqm, List<String> synj, List<String> bjmc,
			String xjhxm) {


		Map<String,Object> params = new HashMap<String,Object>();
		params.put("xxdm", xxdm);
		params.put("xn", xn);
		params.put("xqm", xqm);
		params.put("synj", synj);
		params.put("bjmc", bjmc);
		params.put("xjhxm", xjhxm);
		
		List<Map<String, Object>> accounts = selectList("queryParentAccountList", params);
		
		return accounts;
	}

	@Override
	public List<Map<String, Object>> getSetOtherManagerList(String xxdm,
			String roleDm, String zghxm) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("xxdm", xxdm);
		params.put("roleDm", roleDm);
		params.put("zghxm", zghxm);
		List<Map<String, Object>> manages = selectList("querySetManagerList2", params);
		return manages;
	}

	@Override
	public int deleteOtherManagerList(String roleDm, String zgh) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("roleDm", roleDm);
		params.put("zgh",zgh);
		int result = delete("delManagerList2", params);
		return result;
	}

	@Override
	public List<Map<String, Object>> getTeacherQXXX2(List<String> zgh,String roleDm) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("zgh", zgh);
		params.put("roleDm", roleDm);
		List<Map<String, Object>> qxxx = selectList("getTeacherQXXX2", params);
		
		return qxxx;
	}

	
	

}
