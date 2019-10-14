package com.talkweb.csbasedata.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.tools.MD5Util;
import com.talkweb.csbasedata.dao.StudentBaseManageDao;

@Repository
public class StudentBaseManageDaoImpl extends MyBatisBaseDaoImpl implements StudentBaseManageDao {
	private static final Logger logger = LoggerFactory.getLogger(TeacherManageDaoImpl.class);
	@Override
	public List<JSONObject> getAccountObj(JSONObject param) {
		return selectList("getAccountObjSTUDENT",param);
	}

	@Override
	public void resetAdminPassword(JSONObject param) {
		 update("resetAdminPasswordSTUDENT",param);
	}

	@Override
	public void deleteAccount(JSONObject param) {
		update("deleteAccountSTUDENT",param);
	}

	@Override
	public void deleteUser(JSONObject param) {
		update("deleteUserSTUDENT",param);
	}

	@Override
	public void deleteParent(JSONObject param) {
		update("deleteParentSTUDENT",param);
	}

	@Override
	public void deleteStudent(JSONObject param) {
		update("deleteStudentSTUDENT",param);
	}

	@Override
	public JSONObject getStudentInfo(JSONObject param) {
		return selectOne("getStudentInfoSTUDENT",param);
	}

	@Override
	public JSONObject getStudentGradeAndClass(JSONObject param) {
		return selectOne("getStudentGradeAndClassSTUDENT",param);
	}

	@Override
	public List<JSONObject> getStudentList(JSONObject param) {
		return selectList("getStudentListSTUDENT",param);
	}

	@Override
	public List<JSONObject> getParentList(JSONObject param) {
		return selectList("getParentListSTUDENT",param);
	}
	@Override
	public List<JSONObject> getParentStudentList(JSONObject param) {
		return selectList("getParentStudentListSTUDENT",param);
	}
	@Override
	public List<String> getStudentIds(JSONObject param) {
		return selectList("getStudentIdsSTUDENT",param);
	}

	@Override
	public void deleteUserExtend(JSONObject param) {
		update("deleteUserExtendSTUDENT",param);
	}

	@Override
	public long insertUser(JSONObject param) {
		return update("insertUserSTUDENT",param);
	}

	@Override
	public long insertAccount(JSONObject param) {
		return update("insertAccountSTUDENT",param);
	}

	@Override
	public void insertParent(JSONObject param) {
		update("insertParentSTUDENT",param);
	}

	@Override
	public void insertStudent(JSONObject param) {
		update("insertStudentSTUDENT",param);
	}

	@Override
	public void insertUserExtend(JSONObject param) {
		update("insertUserExtendSTUDENT",param);
	}

	@Override
	public void updateAccountPwd(JSONObject param) {
		update("updateAccountPwdSTUDENT",param);
	}

	@Override
	public void updateParentAccount(JSONObject param) {
		update("updateParentAccountSTUDENT",param);
	}

	@Override
	public void updateStudentAccount(JSONObject param) {
		update("updateStudentAccountSTUDENT",param);
	}

	@Override
	public void updateStudent(JSONObject param) {
		update("updateStudentSTUDENT",param);
	}
	
	@Override
	public int insertImportStudentBatch(Map<String, Object> needInsert) {
		logger.info("studentImport start insertImportTeacherBatch......."+needInsert);
		List<JSONObject> insertStudentList = (List<JSONObject>) needInsert.get("insertStudentList");
		List<JSONObject> insertStudentAccountList = (List<JSONObject>) needInsert.get("insertStudentAccountList");
		List<JSONObject> insertStudentUserList = (List<JSONObject>) needInsert.get("insertStudentUserList");
		List<JSONObject> insertParentAccountList = (List<JSONObject>) needInsert.get("insertParentAccountList");
		List<JSONObject> insertParentUserList = (List<JSONObject>) needInsert.get("insertParentUserList");
		List<JSONObject> insertParentList = (List<JSONObject>) needInsert.get("insertParentList");
		List<JSONObject> insertUserExtendList =(List<JSONObject>) needInsert.get("insertUserExtendList");
		Map<String,JSONObject> needUpdatePswAll =  (Map<String, JSONObject>) needInsert.get("needUpdatePswAll");
		List<String> parentAccountNamePhones = (List<String>) needInsert.get("parentAccountNamePhones");
		List<JSONObject> updateUserExtendList = (List<JSONObject>) needInsert.get("updateUserExtendList");
		List<JSONObject> updateStudentAccountList = (List<JSONObject>) needInsert.get("updateStudentAccountList");
		List<JSONObject> updateStudentList = (List<JSONObject>) needInsert.get("updateStudentList");
		List<JSONObject> updateParentClassIdList = (List<JSONObject>) needInsert.get("updateParentClassIdList");
		List<JSONObject> updateParentAccountList = (List<JSONObject>) needInsert.get("updateParentAccountList");
		JSONObject deleteUserExtendList =  (JSONObject) needInsert.get("deleteUserExtendObj");
		if(parentAccountNamePhones!=null){
			Set<String> parentAccountNamePhoneSet = new HashSet<>(parentAccountNamePhones);
			parentAccountNamePhones.addAll(parentAccountNamePhoneSet);
		}
		List<String> ids = new ArrayList<String>();
		List<String> userIds = new ArrayList<String>();
		//找到UUID对应的id
		for (Map.Entry<String, JSONObject> entry : needUpdatePswAll.entrySet()) 
		{
			JSONObject obj = entry.getValue();
			String studentUUID = obj.getString("studentUUID");
			String accountUUID = obj.getString("accountUUID");
			String parentUUID = obj.getString("parentUUID");
			String parentAccountUUID = obj.getString("parentAccountUUID");
			ids.add(accountUUID);
			ids.add(parentAccountUUID);
			userIds.add(studentUUID);
			userIds.add(parentUUID);
		}
		//一、处理插入的
			//1.插入 学生和家长的account和user
		logger.info("studentImport start 1.......");
		int insertNum = 0;
		if(insertStudentAccountList.size()>0){
		  insertNum = update("insertAccountBatchSTUDENT",insertStudentAccountList);
		}
		logger.info("studentImport start 2.......");
		//家长的phone是否作为accountName
		//判断重复
		JSONObject json = new JSONObject();
		json.put("ids", parentAccountNamePhones);
		List<JSONObject> accountNames = new ArrayList<JSONObject>();
		List<JSONObject> accountNames1 = new ArrayList<JSONObject>();
		if(parentAccountNamePhones.size()>0){
			for(String an:parentAccountNamePhones){
				if("null".equals(an)||an==null||StringUtils.isBlank(an)){
					continue;
				}
			 accountNames1 = selectList("getAccountObjSTUDENT",json);
			 if(accountNames1!=null && accountNames1.size()>0){
				 accountNames.addAll(accountNames1);
			 }
			}	
		}
		logger.info("studentImport start 3.......");
		Map<String,String> repeatAccountNameMap = new HashMap<String,String>();
		List<String> repeatIds = new ArrayList<String>();
		for(JSONObject accountNameObj:accountNames){
			String accountName = accountNameObj.getString("accountName");
			repeatAccountNameMap.put(accountName, accountName);
		}
		for(JSONObject insertParentAccountObj:insertParentAccountList){
			String mobilePhone = insertParentAccountObj.getString("mobilePhone");
			if(!repeatIds.contains(mobilePhone) &&!repeatAccountNameMap.containsKey(mobilePhone) && StringUtils.isNotBlank(mobilePhone) ){
				insertParentAccountObj.put("accountName", mobilePhone);
				repeatIds.add(mobilePhone);
			}
		}
		if(insertParentAccountList.size()>0){
			update("insertAccountBatchSTUDENT",insertParentAccountList);
		}
		logger.info("studentImport start 4.......");
		  
			//2.处理更新密码
		  JSONObject obj = new JSONObject();
		  List<JSONObject> aList = new ArrayList<JSONObject>();
		  if(ids.size()>0){
			  obj.put("ids", ids);
			  aList = selectList("getAccountListSTUDENT",obj);
		  }
		  logger.info("studentImport start 5.......");
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
		  if(needUpdatePswList.size()>0){
				  update("updateAccountPWDBatchSTUDENT",needUpdatePswList);
		  }
		  logger.info("studentImport start 6.......");
			//3.处理更新各自增id
		  for(JSONObject studentUser:insertStudentUserList){
			  String accountId = studentUser.getString("accountId");
			  accountId = uuidIdMap.get(accountId);
			  studentUser.put("accountId", accountId);
		  }
		  for(JSONObject parentUser:insertParentUserList){
			  String accountId = parentUser.getString("accountId");
			  accountId = uuidIdMap.get(accountId);
			  parentUser.put("accountId", accountId);
		  }
		  if(insertStudentUserList.size()>0){
				update("insertUserBatchSTUDENT",insertStudentUserList);
			}  
		  logger.info("studentImport start 7.......");
			if(insertParentUserList.size()>0){
				update("insertUserBatchSTUDENT",insertParentUserList);
			}  
			 logger.info("studentImport start 8.......");
		  List<JSONObject> uList = new ArrayList<JSONObject>();
		  if(userIds.size()>0){
			  obj.put("ids", userIds);
		  	  uList = selectList("getUserListSTUDENT",obj);
		  }
		  logger.info("studentImport start 9.......");
		  if(uList!=null){
			  for(JSONObject u :uList){
				  String uuid = u.getString("uuid");
				  String userId = u.getString("userId");
				  uuidIdMap.put(uuid, userId);
			  }
		  }
		  List<JSONObject> insertStudentListTrue = new ArrayList<JSONObject>();
		  for(JSONObject insertStudent :insertStudentList){
			  String studentUserUUID = insertStudent.getString("userId");
			  String userId = uuidIdMap.get(studentUserUUID);
			  String studentAccountUUID = insertStudent.getString("accountId");
			  String accountId = uuidIdMap.get(studentAccountUUID);
			  if(StringUtils.isBlank(userId)||StringUtils.isBlank(accountId) ){
				  continue;
			  }
			  insertStudent.put("accountId", accountId);
			  insertStudent.put("userId", userId);
			  insertStudentListTrue.add(insertStudent);
		  }
		 
		  if(insertStudentListTrue.size()>0){
			  update("insertStudentBatchSTUDENT",insertStudentListTrue);
		  }
		  logger.info("studentImport start 10.......");
		  List<JSONObject> insertParentListTrue = new ArrayList<JSONObject>();
		  for(JSONObject studentUser:insertParentList){
			 String studentUserUUID =  studentUser.getString("studentUserUUID");
			 String parentUserUUID = studentUser.getString("parentUserId");
			 String userId = uuidIdMap.get(studentUserUUID);
			 String parentUserId = uuidIdMap.get(parentUserUUID);
			  if(StringUtils.isBlank(userId)||StringUtils.isBlank(parentUserId) ){
				  continue;
			  }
			 studentUser.remove("studentUserUUID");
			 studentUser.put("userId", userId);
			 studentUser.put("parentUserId", parentUserId);
			 insertParentListTrue.add(studentUser);
		  }
		  if(insertParentListTrue.size()>0){
			  update("insertParentBatchSTUDENT",insertParentListTrue);
		  }
		  logger.info("studentImport start 11.......");
		  List<JSONObject> insertUserExtendListTrue = new ArrayList<JSONObject>();
		  for(JSONObject userExtend:insertUserExtendList){
			  String accountId = userExtend.getString("accountId");
			  String userId = userExtend.getString("userId");
			  accountId  =uuidIdMap.get(accountId);
			  userId = uuidIdMap.get(userId);
			  if(StringUtils.isBlank(accountId)||StringUtils.isBlank(userId) ){
				  continue;
			  }
			  userExtend.put("accountId", accountId);
			  userExtend.put("userId", userId);
			  insertUserExtendListTrue.add(userExtend);
		  }
		  if(insertUserExtendListTrue.size()>0){
				update("insertUserExtendBatchSTUDENT",insertUserExtendListTrue);
		  }
		  logger.info("studentImport start 12.......");
		//二、处理更新的
		  //更新学生姓名、性别
		  if(updateStudentAccountList.size()>0){
				  update("updateAccountBatchSTUDENT",updateStudentAccountList);
				  insertNum+=updateStudentAccountList.size();
		  }
		  //更新学籍号、学号、座位号、年级、班级
		  if(updateStudentList.size()>0){
				  update("updateStudentBatchSTUDENT",updateStudentList);
		  }
		  //更新家长表中的classId
		  if(updateParentClassIdList.size()>0){
				  update("updateParentBatchSTUDENT",updateParentClassIdList);
		  }
		  //更新电子卡号
		  if(deleteUserExtendList.size()>0){
			  if(((List<String>)deleteUserExtendList.get("ids")).size()>0){
				  update("deleteUserExtendBatchSTUDENT",deleteUserExtendList);
			  }
		  }
		  if(updateUserExtendList.size()>0){
				  update("insertUserExtendBatchSTUDENT",updateUserExtendList);
		  }
		  //更新家长电话号码
		  if(updateParentAccountList.size()>0){
				  update("updateParentAccountPhoneBatchSTUDENT",updateParentAccountList);
		  }
		  return insertNum;
	}
	@Override
	public List<JSONObject> getUserExtend(JSONObject ue) {
		return selectList("getUserExtendSTUDENT",ue);
	}

	@Override
	public List<JSONObject> getStudentAccountObj(JSONObject json) {
		return selectList("getStudentAccountObjSTUDENT",json);
	}

	@Override
	public List<JSONObject> getGradeListBySchoolId(JSONObject obj) {
		return selectList("getGradeListBySchoolIdSTUDENT",obj);
	}

	@Override
	public List<JSONObject> getClassListBySchoolId(JSONObject obj) {
		return selectList("getClassListBySchoolIdSTUDENT",obj);
	}

	@Override
	public void updateParentClassId(JSONObject param) {
		update("updateParentClassIdSTUDENT",param);
	}

	@Override
	public List<JSONObject> getStudentListBySchoolId(JSONObject obj) {
		return selectList("getStudentListBySchoolIdSTUDENT",obj);
	}

	@Override
	public List<JSONObject> getAccountByUserId(JSONObject obj) {
		return selectList("getAccountByUserIdSTUDENT",obj);
	}

	@Override
	public int getStudentTotal(JSONObject param) {
		return selectOne("getStudentTotalSTUDENT",param);
	}

	@Override
	public List<JSONObject> getUserExtendName(JSONObject ue) {
		return selectList("getUserExtendNameSTUDENT",ue);
	}

	@Override
	public List<JSONObject> getStudentRepeatList(JSONObject sr) {
		return selectList("getStudentRepeatListSTUDENT",sr);
	}

}