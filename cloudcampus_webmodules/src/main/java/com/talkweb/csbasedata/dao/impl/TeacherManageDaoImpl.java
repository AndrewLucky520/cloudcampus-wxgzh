package com.talkweb.csbasedata.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.exolab.castor.types.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.tools.MD5Util;
import com.talkweb.csbasedata.dao.TeacherManageDao;

@Repository
public class TeacherManageDaoImpl extends MyBatisBaseDaoImpl implements TeacherManageDao {
	private static final Logger logger = LoggerFactory.getLogger(TeacherManageDaoImpl.class);
	@Override
	public int getTeacherTotal(long schoolId) {
		return selectOne("getTeacherTotal",schoolId);
	}

	@Override
	public List<JSONObject> getTeacherList(JSONObject param) {
		return selectList("getTeacherList",param);
	}

	@Override
	public int deleteTeacherCourse(JSONObject param) {
		return delete("deleteTeacherCourse",param);
	}

	@Override
	public int addTeacherCourses(List<JSONObject> list) {
		return insert("insertTeacherCourses",list);
	}
	
	@Override
	public String getAccountNameById(long accountId) {
		return selectOne("getAccountNameById",accountId);
	}

	@Override
	public int resetTeacherPassword(JSONObject param) {
		return update("updateTeacherPassword",param);
	}

	@Override
	public int addAccountByTeacher(JSONObject param) {
		return insert("insertAccountByTeacher",param);
	}

	@Override
	public int updateAccountByTeacher(JSONObject param) {
		return update("updateAccountByTeacher",param);
	}

	@Override
	public int addUserByTeacher(JSONObject param) {
		return insert("insertUserByTeacher",param);
	}

	@Override
	public int addTeacher(JSONObject param) {
		return insert("insertTeacher",param);
	}

	@Override
	public JSONObject getTeacherInfo(JSONObject param) {
		return selectOne("getTeacherInfo",param);
	}

	@Override
	public JSONObject getNamesFromAccount(JSONObject json) {
		return selectOne("getNamesFromAccount",json);
	}

	@Override
	public List<JSONObject> getNamesMapBySchoolId(long schoolId) {
		return selectList("getNamesMapBySchoolId",schoolId);
	}

	@Override
	public void deleteInfoByAccountIds(List<Long> list) {
		delete("deleteAccountById",list);
		delete("deleteUserById",list);
	}

	@Override
	public void deleteInfoByTeacherIds(List<Long> list) {		
		delete("deleteTeacherById",list);
		delete("deleteTeacherCourses",list);
		delete("deleteSchoolManager",list);
		delete("deleteOrgMember",list);
		delete("deleteOrgHeader",list);
		List<JSONObject> cList = selectList("getHeadTeacherClassListTEACHER",list);
		if(cList.size()>0){
			update("updateClassHeadTeacherIdTEACHER",cList);
		}
	}

	@Override
	public List<JSONObject> getTeacherListBySchoolId(JSONObject param) {
		return selectList("getTeacherListBySchoolIdTEACHER",param);
	}
	@Override
	public List<JSONObject> getAccountObj(JSONObject json) {
		return selectList("getAccountObjTEACHER",json);
	}
	@Override
	public int insertImportTeacherBatch(Map<String, Object> needInsert) {
		logger.info("90progress start insertImportTeacherBatch......."+needInsert);
		List<JSONObject> insertTeacherAccountList = (List<JSONObject>) needInsert.get("insertTeacherAccountList");
		List<JSONObject> insertTeacherUserList = (List<JSONObject>) needInsert.get("insertTeacherUserList");
		List<JSONObject> insertTeacherList = (List<JSONObject>) needInsert.get("insertTeacherList");
		Map<String,JSONObject> needUpdatePswAll =  (Map<String, JSONObject>) needInsert.get("needUpdatePswAll");
		List<JSONObject> updateTeacherAccountList = (List<JSONObject>) needInsert.get("updateTeacherAccountList");
		List<String>  teacherAccountNamePhones = (List<String>) needInsert.get("teacherAccountNamePhones");
		if(teacherAccountNamePhones!=null){
			Set<String> teacherAccountNamePhoneSet = new HashSet<>(teacherAccountNamePhones);
			teacherAccountNamePhones.addAll(teacherAccountNamePhoneSet);
		}
		List<String> ids = new ArrayList<String>();
		List<String> userIds = new ArrayList<String>();
		//找到UUID对应的id
		for (Map.Entry<String, JSONObject> entry : needUpdatePswAll.entrySet()) 
		{
			JSONObject obj = entry.getValue();
			String userUUID = obj.getString("userUUID");
			String accountUUID = obj.getString("accountUUID");
			ids.add(accountUUID);
			userIds.add(userUUID);
		}
		logger.info("90progress start deal with......."+new DateTime());
		long time1=new Date().getTime();
		//一、处理插入的
			//1.插入老师的account和user
		int insertNum = 0;
		//老师的phone是否作为accountName
		//判断重复
		JSONObject json = new JSONObject();
		//json.put("ids", teacherAccountNamePhones);
		List<JSONObject> accountNames = new ArrayList<JSONObject>();
		List<JSONObject> accountNames1 = new ArrayList<JSONObject>();
		long time=0;
		if(teacherAccountNamePhones.size()>0){
			 time=new Date().getTime();
			logger.info("90progress start deal with..0....."+(time-time1)+"==>"+teacherAccountNamePhones.toString());
			for(String an:teacherAccountNamePhones){
				if("null".equals(an)||an==null||StringUtils.isBlank(an)){
					continue;
				}
				json.put("accountName", an);
				accountNames1 = selectList("getAccountObjTEACHER",json);
				if(accountNames1!=null && accountNames1.size()>0){
					accountNames.addAll(accountNames1);
				}
			}
		}
		long time2=new Date().getTime();
		logger.info("90progress start deal with..1....."+(time2-time)+"==>size:"+teacherAccountNamePhones.size());
		Map<String,String> repeatAccountNameMap = new HashMap<String,String>();
		List<String> repeatIds = new ArrayList<String>();
		if(accountNames!=null){
			for(JSONObject accountNameObj:accountNames){
				String accountName = accountNameObj.getString("accountName");
				repeatAccountNameMap.put(accountName, accountName);
			}
		}
		long time3=new Date().getTime();
		logger.info("90progress start deal with..2....."+(time3-time2));
		for(JSONObject insertTeacherAccountObj:insertTeacherAccountList){
			String mobilePhone = insertTeacherAccountObj.getString("mobilePhone");
			if(!repeatIds.contains(mobilePhone) &&!repeatAccountNameMap.containsKey(mobilePhone) && StringUtils.isNotBlank(mobilePhone) ){
				insertTeacherAccountObj.put("accountName", mobilePhone);
				repeatIds.add(mobilePhone);
			}
		}
		long time4=new Date().getTime();
		logger.info("90progress start deal with..3....."+(time4-time3));
		if(insertTeacherAccountList.size()>0){
			  insertNum = update("insertAccountBatchTEACHER",insertTeacherAccountList);
		}
		long time5=new Date().getTime();
		logger.info("90progress start updatepsd......."+(time5-time4));
			//2.处理更新密码
		  JSONObject obj = new JSONObject();
		  List<JSONObject> aList = new ArrayList<JSONObject>();
		  if(ids.size()>0){
			  obj.put("ids", ids);
			  logger.info("90progress selectList-getAccountListTEACHER"+obj.toJSONString());
			  aList = selectList("getAccountListTEACHER",obj);
		  }
		  long time6=new Date().getTime();
		  logger.info("90progress start updatepsd...1...."+(time6-time5));
		  List<JSONObject> needUpdatePswList = new ArrayList<JSONObject>();
		  //存放account和user表的 id和uuid映射
		  Map<String,String> uuidIdMap = new HashMap<String, String>(); //UUID-ACCOUNTID or USERID
		  if(aList!=null){
			  for(JSONObject a:aList){
				  String accountId = a.getString("accountId");
				  String accountName = a.getString("accountName");
				  String uuid = a.getString("uuid");
				  uuidIdMap.put(uuid, accountId); 
				  //截后六位
				  String pwdQZ=accountName.substring(accountName.length()-6,accountName.length());
				  //加密
				  String pwd = MD5Util.getMD5String(MD5Util.getMD5String(pwdQZ)+accountId);
				  JSONObject needUpdatePswObj = new JSONObject();
				  needUpdatePswObj.put("pwd", pwd);
				  needUpdatePswObj.put("id", accountId);
				  needUpdatePswList.add(needUpdatePswObj);
			  }
		  }
		  long time7=new Date().getTime();
		  logger.info("90progress start updatepsd...2...."+(time7-time6));
		  if(needUpdatePswList.size()>0){
				  update("updateAccountPWDBatchTEACHER",needUpdatePswList);
		  }
		  long time8=new Date().getTime();
		  logger.info("90progress start updatepsd...3...."+(time8-time7));
		  logger.info("90progress start updateid......."+(time8-time7));
			//3.处理更新各自增id
		  for(JSONObject teacherUser:insertTeacherUserList){
			  String accountId = teacherUser.getString("accountId");
			  accountId = uuidIdMap.get(accountId);
			  teacherUser.put("accountId", accountId);
		  }
		  long time9=new Date().getTime();
		  logger.info("90progress start updateid...1..."+(time9-time8));
		  if(insertTeacherUserList.size()>0){
				update("insertUserBatchTEACHER",insertTeacherUserList);
			}  
		  long time10=new Date().getTime();
		  logger.info("90progress start updateid...2..."+(time10-time9));
		  List<JSONObject> uList = new ArrayList<JSONObject>();
		  if(userIds.size()>0){
			  obj.put("ids", userIds);
			  logger.info("90progress selectList-getUserListSTUDENT"+obj.toJSONString());
		  	  uList = selectList("getUserListSTUDENT",obj);
		  }
		  long time11=new Date().getTime();
		  logger.info("90progress start updateid...3..."+(time11-time10));
		  if(uList!=null){
			  for(JSONObject u :uList){
				  String uuid = u.getString("uuid");
				  String userId = u.getString("userId");
				  uuidIdMap.put(uuid, userId);
			  }
		  }
		  for(JSONObject teacherUser:insertTeacherList){
			  String teacherId = teacherUser.getString("uuid");
			  teacherId = uuidIdMap.get(teacherId);
			  teacherUser.put("teacherId", teacherId);
		  }
		  long time12=new Date().getTime();
		  logger.info("90progress start updateid...4..."+(time12-time11));
			if(insertTeacherList.size()>0){
				update("insertTeacherBatchTEACHER",insertTeacherList);
			}  
			long time13=new Date().getTime();
			 logger.info("90progress start updateid...5..."+(time13-time12));
		//二、处理更新的
			logger.info("90progress start updatename and others ......."+new DateTime());
		  //更新老师姓名、性别、电话号码
		  if(updateTeacherAccountList.size()>0){
				  update("updateAccountBatchTEACHER",updateTeacherAccountList);
				  insertNum+=updateTeacherAccountList.size();
		  }
		  return insertNum;
	}

	
	 
}