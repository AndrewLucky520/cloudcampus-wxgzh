package com.talkweb.commondata.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.MD5Util;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.AdminManageDao;
import com.talkweb.commondata.service.AdminManageService;
import com.talkweb.commondata.util.PermissionUtil;



/**
 * 管理员实现-serviceImpl
 * @author zhh
 *
 */
@Service("adminManageService")
public class AdminManageServiceImpl implements AdminManageService{
	@Autowired
	private AdminManageDao adminManageDao;
	//private static final boolean ISNEWENTREANCE = true;
	private static final Logger logger =  LoggerFactory.getLogger(AdminManageServiceImpl.class);
/**
 * 管理员
 * @author zhh
 */
	
	@Override
	public List<JSONObject> getAdministratorList(JSONObject obj)
			throws Exception {
		String from = obj.getString("from");
		List<JSONObject> aList = null ;
		if("1".equals(from)){
		    aList = adminManageDao.getAdministratorListTeacher(obj);
		}else{
		    aList = adminManageDao.getAdministratorList(obj);
		}
		//获取所有的管理员userId
		List<String> uList = null;
		if("1".equals(from)){
		     uList = adminManageDao.getAllUserTeacherList(obj);
		}else{
			  uList = adminManageDao.getAllUserManagerList(obj);
		}
		//获取所有userId对应的菜单
		logger.info("permission uList:"+uList.toString());
		obj.put("ids", uList);
		List<JSONObject> upList = adminManageDao.getUserPermissonList(obj);
		logger.info("permission upList:"+upList.toString());
		Map<String,List<String>> upMap = new HashMap<String, List<String>>();
		for(JSONObject up:upList){
			String userId = up.getString("userId");
			String name = up.getString("name");
			if(StringUtils.isBlank(name)|| StringUtils.isBlank(userId)){continue;}
			if(upMap.containsKey(userId)){
				List<String> nameList = upMap.get(userId);
				if(nameList!=null && !nameList.contains(name)){
					nameList.add(name);
				}
				upMap.put(userId, nameList);
			}else{
				List<String> nameList = new ArrayList<String>();
				nameList.add(name);
				upMap.put(userId, nameList);
			}
		}
		logger.info("permission uMap:"+upMap.toString());
		List<JSONObject> returnAList = new ArrayList<JSONObject>();
		if(aList!=null){
			for(JSONObject a:aList){
				String userId = a.getString("userId");
				String accountStatus = a.getString("accountStatus");
				a.put("accountStatus", "未激活");
				if("1".equals(accountStatus)){
					a.put("accountStatus", "正常");
				}
				a.put("position", "");
				List<String> nameList = upMap.get(userId);
				String name="";
				if("1".equals(from)){
					if(nameList==null||nameList.size()<1){
						continue;
					}
				}
				if(nameList!=null && nameList.size()>0){
					for(String n:nameList){
						if(StringUtils.isNotBlank(n)){
							name+=n+",";
						}
					}
					if(StringUtils.isNotBlank(name)){
						name=name.substring(0,name.length()-1);
					}
					if(StringUtils.isNotBlank(name)){
						a.put("position", name);
					}
				}
				String hasAllRight = a.getString("hasAllRight");
				if("1".equals(hasAllRight)){
					a.put("position", "最高权限");
				}
				a.put("hasAllRight", hasAllRight);
				returnAList.add(a);
			}
		}
		return returnAList;
	}
	@Override
	public int updateAdministrator(JSONObject obj) throws Exception {
		String from = obj.getString("from");
		String schoolId = obj.getString("schoolId");
		String oldAccountId = obj.getString("oldAccountId");
		String accountId = obj.getString("accountId");
		//获取管理员的user身份
		obj.put("isNotQuerySchoolManager", 1); //不级联查询学校schoolmanager
		List<JSONObject> adminObjList = adminManageDao.getUserByAccountId(obj);
		logger.info("getUserByAccountId:"+adminObjList.toString()+"  obj:"+obj.toJSONString());
		String userId = "";
		if(adminObjList!=null && adminObjList.size()>0 && adminObjList.get(0)!=null && StringUtils.isNotBlank(adminObjList.get(0).getString("userId")) ){
			userId = adminObjList.get(0).getString("userId");
			obj.put("userId", userId);
			//判断是否重复设置了已设置的管理员
			if("1".equals(from)){
				List<JSONObject> upList = adminManageDao.getUserPermissonList(obj); //查询up表中是否有权限信息
				if(accountId!=null && !accountId.equals(oldAccountId) && upList!=null && upList.size()>0){
					return -1;
				}
			}else{
			   JSONObject sm = adminManageDao.getSchoolManager(obj);
			   if(accountId!=null && !accountId.equals(oldAccountId) && sm!=null){
					return -1;
				}
			}
			
			
		}
		//旧id不为空且旧id不等于新的id 则清空旧管理员的所有信息
		if(StringUtils.isNotBlank(oldAccountId) && accountId !=null && !accountId.equals(oldAccountId)){ 
			//获取旧人的user身份
			JSONObject param = new JSONObject();
			List<String> ids = new ArrayList<String>();
			ids.add(oldAccountId);
			param.put("ids", ids);
			param.put("role", obj.getString("role"));
			param.put("schoolId",schoolId);
			param.put("from", from);
			//删除旧人的管理员身份和权限信息等
			this.deleteAdministrator(param);
		}else if(StringUtils.isNotBlank(oldAccountId) && accountId!=null && accountId.equals(oldAccountId)){
			//清除旧管理员的权限信息
			JSONObject updateParam = new JSONObject();
			updateParam.put("schoolId", schoolId);
			updateParam.put("accountId", oldAccountId);
			updateParam.put("role", obj.getString("role"));
			updateParam.put("termInfoId", obj.getString("termInfoId"));
			if("1".equals(from)){
				updateParam.put("isNotQuerySchoolManager", 1); 
			}
			List<JSONObject> oldAdminObjList = adminManageDao.getUserByAccountId(updateParam);
			if(oldAdminObjList!=null && oldAdminObjList.size()>0 && StringUtils.isNotBlank(oldAdminObjList.get(0).getString("userId"))){
				JSONObject oldAdminObj =oldAdminObjList.get(0);
				String oldUserId = oldAdminObj.getString("userId");
				updateParam.put("userId", oldUserId);
				if(StringUtils.isNotBlank(oldUserId)){
					adminManageDao.deletePermissonspc(updateParam);
				}
			}
		}
		if(!"1".equals(from)){
			//新增管理员身份
			if(accountId!=null && !accountId.equals(oldAccountId)){
				obj.put("uuid", UUIDUtil.getUUID());
				obj.put("hasAllRight", 0);
				//obj.put("createTime", new Date().getTime());
				adminManageDao.insertUser(obj);
				userId = obj.getString("id");
				obj.put("userId", userId);
				adminManageDao.insertSchoolManager(obj);
			}else{
				//更新管理员基本信息
				adminManageDao.updateSchoolManager(obj);
			}
		}
		//新增权限信息
		List<String> navIds = (List<String>) obj.get("navIds");
		/*Map<String,String> pMap = new HashMap<String, String>();
		if(PermissionUtil.ISNEWENTREANCE){
			pMap = PermissionUtil.permissonNewSchoolMap;
		}else{ //非新高考
			pMap = PermissionUtil.permissonNormalMap;
		}*/
		Map<String,String> pMap = new HashMap<String, String>();
		obj.put("navType", PermissionUtil.NAVTYPE);//目前只有一个模板
		List<JSONObject> navList = adminManageDao.getNavList(obj);
		for(JSONObject navObj:navList){
			pMap.put(navObj.getString("navId"), navObj.getString("navId"));
		}
		
		List<JSONObject> list = new ArrayList<JSONObject>();
		if(navIds!=null){
			for(String navId:navIds){
				if(pMap.get(navId)==null){
					continue;
				}
				JSONObject json = new JSONObject();
				json.put("userId", userId);
				json.put("navId", navId);
				//json.put("permissiontype",pMap.get(navId));
				list.add(json);
			}
		}
		if(list!=null && list.size()>0){
			JSONObject json = new JSONObject();
			json.put("termInfoId", obj.getString("termInfoId"));
			json.put("list", list);
			adminManageDao.insertPermissionspcBatch(json);
		}
		
		return 1;
	}
	@Override
	public int resetAdminPassword(JSONObject obj) throws Exception {
		JSONObject aObj = adminManageDao.getAccountObj(obj);
		//获取登录账户
		String accountId = "";
		String accountName = "";
		if(aObj!=null){
			accountId = aObj.getString("id");
			accountName = aObj.getString("accountName");
		}
		if(StringUtils.isNotBlank(accountName) && StringUtils.isNotBlank(accountId)){
			//截后六位
			accountName=accountName.substring(accountName.length()-7,accountName.length()-1);
			//加密
			String pwd = MD5Util.getMD5String(MD5Util.getMD5String(accountName)+accountId);
			obj.put("pwd", pwd);
			adminManageDao.resetAdminPassword(obj);
		}
		return 1;
	}
	@Override
	public int deleteAdministrator(JSONObject obj) throws Exception {
		String from = obj.getString("from");
		if("1".equals(from)){
			obj.put("isNotQuerySchoolManager", 1);
		}
		List<JSONObject> userList = adminManageDao.getUserByAccountId(obj);
		logger.info("deletepermission userList:"+userList.toString()+" obj:"+obj.toJSONString());
		List<String> userIds = new ArrayList<String>();
		if(userList!=null){
			for(JSONObject user:userList){
				String userId = user.getString("userId");
				if(StringUtils.isNotBlank(userId)){
					if(!userIds.contains(userId))
					{
						userIds.add(userId);
					}
				}
			}
		}
		obj.put("userIds", userIds);
		logger.info("deletepermission userIds:"+userIds.toString());
		if(userIds.size()>0){
			if(!"1".equals(from)){ //如果是从云平台取 则不需要删除schoolmanager表
				adminManageDao.deleteSchoolManager(obj);
				adminManageDao.deleteSchoolManagerUser(obj);
			}
			adminManageDao.deletePermissonspc(obj);
		}
		return 1;
	}
	@Override
	public List<JSONObject> getAdministratorSelectList(JSONObject obj)
			throws Exception {
		String from = obj.getString("from");
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		if("1".equals(from)){
			returnList = adminManageDao.getAdministratorSelectListByTeacherPermission(obj);
		}else{
			returnList = adminManageDao.getAdministratorSelectList(obj);
		}
		//去除已经有管理员角色的老师，即下拉不展现
		return returnList ; 
	}
	@Override
	public List<JSONObject> getAdministrator(JSONObject obj) throws Exception {
		String from = obj.getString("from");
		String accountId = obj.getString("accountId");
		List<String> selectedNavList = new ArrayList<String>();
		if(StringUtils.isNotBlank(accountId)){ //非刚刚创建
			//获取对应的权限信息 
			if("1".equals(from)){
				obj.put("isNotQuerySchoolManager", 1);
			}
			List<JSONObject> userList = adminManageDao.getUserByAccountId(obj);
			if(userList!=null && userList.size()>0 && userList.get(0)!=null && StringUtils.isNotBlank(userList.get(0).getString("userId"))){
				JSONObject user = userList.get(0);
				String userId = user.getString("userId");
				obj.put("userId", userId);
				List<JSONObject> upList = adminManageDao.getUserPermissonList(obj);
				if(upList!=null){
					for(JSONObject up:upList){
						String navId = up.getString("permissionid");
						if(StringUtils.isNotBlank(navId)){
							if(!selectedNavList.contains(navId)){
								selectedNavList.add(navId);
							}
						}
					}
				}
			}
		}
		//获取菜单列表
		 //判断是否为新高考学校
		obj.put("navType", PermissionUtil.NAVTYPE);
		logger.info("PermissionUtil.ISNEWENTREANCE===> 2is newschool 1isnormalschool :"+PermissionUtil.NAVTYPE);
		List<JSONObject> navList = adminManageDao.getNavList(obj);
		if(navList!=null){
			for(JSONObject navObj:navList){
				navObj.put("isSelected", 0);
				String parentId = navObj.getString("parentId");
				String navName = navObj.getString("navName");
				String navId = navObj.getString("navId");
				if(StringUtils.isBlank(navName)){
					navObj.put("navName", "");
				}
				if(!"0".equals(parentId) && StringUtils.isNotBlank(navName)){
					navObj.put("navName", navName+"管理员");	
				}
				if(selectedNavList.contains(navId)){
					navObj.put("isSelected", 1);
				}
			}
		}
		logger.info("navList===>:"+navList);
		return navList;
	}
	
}
