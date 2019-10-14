package com.talkweb.csbasedata.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.tools.MD5Util;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.csbasedata.dao.StudentBaseManageDao;
import com.talkweb.csbasedata.service.StudentBaseManageService;
import com.talkweb.csbasedata.util.SortUtil;
import com.talkweb.csbasedata.util.StringUtil;
/**
 * 学生管理实现-serviceImpl
 * @author zhh
 *
 */
@Service("studentManageService")
public class StudentBaseManageServiceImpl implements StudentBaseManageService {
	private static final Logger logger = LoggerFactory.getLogger(StudentBaseManageServiceImpl.class);
	@Autowired
	private StudentBaseManageDao studentManageDao;
/**
 * 学生管理
 * @author zhh
 */
	@Override
	public int getStudentTotal(JSONObject param) throws Exception {
		return studentManageDao.getStudentTotal(param);
	}
	@Override
	public List<JSONObject> getStudentList(JSONObject param) throws Exception {
		String classId = param.getString("classId");
		String gradeId = param.getString("gradeId");
		String condition = param.getString("condition");
		if(StringUtils.isNotBlank(classId)){
			List<String> classIds= Arrays.asList(classId.split(","));
			if(classIds.size()>1){
				param.remove("classId");
			}
		}
		if(StringUtils.isNotBlank(gradeId)){
			List<String> gradeIds= Arrays.asList(gradeId.split(","));
			if(gradeIds.size()>1){
				param.remove("gradeId");
			}
		}
		List<JSONObject> studentList = studentManageDao.getStudentList(param);
		List<String> ids = studentManageDao.getStudentIds(param);
		//获取家长信息
		param.put("ids", ids);
		List<JSONObject> parentIdStudentList = new ArrayList<JSONObject>();
		if(ids.size()>0){
			parentIdStudentList = studentManageDao.getParentStudentList(param);
		}
		List<String>parentIds = new ArrayList<String>();
		Map<String,String> parentStudentMap = new HashMap<String,String>();
		for(JSONObject parentStudentObj:parentIdStudentList){
			String parentId = parentStudentObj.getString("parentId");
			String studentId = parentStudentObj.getString("studentId");
			parentStudentMap.put(parentId, studentId);
			parentIds.add(parentId);
			
		}
		List<JSONObject> parentList = new ArrayList<JSONObject>();
		if(parentIds.size()>0){
			param.put("ids", parentIds);
			parentList = studentManageDao.getParentList(param);
		}
		//学生id-家长obj 
		Map<String,JSONObject> parentMap = new HashMap<String, JSONObject>();
		if(parentList!=null){
			for(JSONObject pObj:parentList){
				String parentUserId = pObj.getString("parentUserId");
				String studentId = parentStudentMap.get(parentUserId);
				parentMap.put(studentId, pObj);
			}
		}
		//返回数据
		Map<String,List<JSONObject>>  gcMap = new LinkedHashMap<String,List<JSONObject>>();
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		List<JSONObject> studentListReturn = new ArrayList<JSONObject>();
		for(JSONObject stuObj:studentList){
			int currentLevel = stuObj.getIntValue("currentLevel");
			String studentName = stuObj.getString("studentName");
			String studentAccountName = stuObj.getString("studentAccountName");
			String gradeName = njName.get(T_GradeLevel.findByValue(currentLevel));
			if(StringUtils.isBlank(gradeName)){
				stuObj.put("gradeName", "");
			}else{
				stuObj.put("gradeName", gradeName);
			}
			String studentId = stuObj.getString("studentId");
			JSONObject pObj = parentMap.get(studentId);
			stuObj.put("parentId", "");
			stuObj.put("parentAccountId", "");
			stuObj.put("parentAccountName", "");
			stuObj.put("parentAccountStatus", "未激活");
			if(pObj!=null){
				String parentMobilePhone = pObj.getString("parentMobilePhone");
				if(StringUtils.isNotBlank(condition) ){
				if(!(studentName!=null && studentName.indexOf(condition)>=0
						||studentAccountName!=null && studentAccountName.indexOf(condition)>=0
						   || parentMobilePhone!=null && parentMobilePhone.indexOf(condition)>=0)){continue;}
				}
				String parentAccountStatus = pObj.getString("parentAccountStatus");
				String parentId = pObj.getString("parentUserId");
				String parentAccountId = pObj.getString("parentAccountId");
				String parentAccountName = pObj.getString("parentAccountName");
				stuObj.put("parentId",parentId);
				stuObj.put("parentAccountId", parentAccountId);
				if(StringUtils.isBlank(parentAccountName)){
					stuObj.put("parentAccountName", "");
				}else{
					stuObj.put("parentAccountName", parentAccountName);
				}
				if("1".equals(parentAccountStatus)){
					stuObj.put("parentAccountStatus", "正常");
				}
			}
			//studentListReturn.add(stuObj);
			if(gcMap.containsKey(gradeName)){
				List<JSONObject> cList = gcMap.get(gradeName);
				cList.add(stuObj);
				gcMap.put(gradeName, cList);
			}else{
				List<JSONObject> cList = new ArrayList<JSONObject>();
				cList.add(stuObj);
				gcMap.put(gradeName, cList);
			}
		}
		for (Map.Entry<String,List<JSONObject>> entry : gcMap.entrySet()) {
			List<JSONObject> cList = entry.getValue();
			SortUtil.sortNameNormalJSONList(cList, "className");
			studentListReturn.addAll(cList);
		}
		return studentListReturn;
	}

	@Override
	public JSONObject getStudentInfo(JSONObject param) throws Exception {
		JSONObject studentObj = studentManageDao.getStudentInfo(param);
		/*Date createTime = studentObj.getDate("createTime");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String createTimeString = sdf.format(createTime);
		studentObj.put("createTime", createTimeString);*/
		//获取学生班级年级
		JSONObject stuObj = studentManageDao.getStudentGradeAndClass(param);
		String classId = "";
		String gradeId = "";
		String className = "";
		int currentLevel = 0;
		if(stuObj!=null){
			 classId = stuObj.getString("classId");
			 gradeId = stuObj.getString("gradeId");
			 className = stuObj.getString("className");
			 currentLevel = stuObj.getIntValue("currentLevel");
		}
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		studentObj.put("classId", classId);
		studentObj.put("gradeId", gradeId);
		studentObj.put("gradeClass", "");
		if(StringUtils.isNotBlank(njName.get(T_GradeLevel.findByValue(currentLevel)))&& StringUtils.isNotBlank(className)){
			studentObj.put("gradeClass", njName.get(T_GradeLevel.findByValue(currentLevel))+className);
		}
		//获取家长手机号
		JSONObject json = new JSONObject();
		String parentAccountId = param.getString("parentAccountId");
		if(StringUtils.isNotBlank(parentAccountId)){
			json.put("accountId", parentAccountId);
			List<JSONObject> parentObjList = studentManageDao.getAccountObj(json);
			if(parentObjList!=null && parentObjList.size()>0){
				JSONObject parentObj = parentObjList.get(0);
				if(parentObj!=null && StringUtils.isNotBlank(parentObj.getString("mobilePhone"))){
					String mobilePhone = parentObj.getString("mobilePhone");
					studentObj.put("parentMobilePhone", mobilePhone);
				}else{
					studentObj.put("parentMobilePhone", "");
				}
			}
		}
		return studentObj;
	}

	@Override
	public void resetStudentPassword(JSONObject param) throws Exception {
		String accountId = param.getString("accountId");
		String parentAccountId = param.getString("parentAccountId");
		if(StringUtils.isBlank(accountId)){//重置家长
			param.put("parentAccountId", parentAccountId);
		}
		List<JSONObject> aObjList = studentManageDao.getAccountObj(param);
		if(aObjList!=null && aObjList.size()>0){
			JSONObject aObj = aObjList.get(0);
			//获取登录账户
			String aId = aObj.getString("id");
			String accountName = aObj.getString("accountName");
			//截后六位
			accountName=accountName.substring(accountName.length()-6,accountName.length());
			//加密
			String pwd = MD5Util.getMD5String(MD5Util.getMD5String(accountName)+aId);
			param.put("pwd", pwd);
			studentManageDao.resetAdminPassword(param);
		}
	}

	@Override
	public void deleteStudent(JSONObject paramObj) throws Exception {
		List<JSONObject> deleteStudents = (List<JSONObject>) paramObj.get("deleteStudents");
		List<Long> deleteStudentUserIds = new ArrayList<Long>();
		List<Long> deleteParentUserIds = new ArrayList<Long>();
		List<Long> deleteStudentAccountIds = new ArrayList<Long>();
		List<Long> deleteParentAccountIds = new ArrayList<Long>();
		if(deleteStudents!=null){
			for(JSONObject sObj:deleteStudents){
				long parentAccountId = sObj.getLongValue("parentAccountId");
				long parentUserId = sObj.getLongValue("parentId");
				long accountId = sObj.getLongValue("accountId");
				long studentId = sObj.getLongValue("studentId");
				if(!deleteStudentAccountIds.contains(accountId) && accountId!=0){
					deleteStudentAccountIds.add(accountId);
				}
				if(!deleteParentAccountIds.contains(parentAccountId) && parentAccountId!=0){
					deleteParentAccountIds.add(parentAccountId);
				}
				if(!deleteStudentUserIds.contains(studentId) && studentId!=0){
					deleteStudentUserIds.add(studentId);
				}
				if(!deleteParentUserIds.contains(parentUserId) && parentUserId!=0){
					deleteParentUserIds.add(parentUserId);
				}
			}
		}
		//删除学生
		long time1=new Date().getTime();
		JSONObject param = new JSONObject();
		if(deleteStudentAccountIds.size()>0){
			if((deleteStudentAccountIds.size()==1)){
				param.put("accountId", deleteStudentAccountIds.get(0));
			}else{
				param.put("ids", deleteStudentAccountIds);
			}
			studentManageDao.deleteAccount(param);
		}
		long time2=new Date().getTime();
		logger.info("deleteOneStudent1==>"+(time2-time1));
		param.clear();
		if(deleteStudentUserIds.size()>0){
			if(deleteStudentUserIds.size()==1){
				param.put("userId", deleteStudentUserIds.get(0));
			}else{
				param.put("ids", deleteStudentUserIds);
			}
			param.put("schoolId", paramObj.getString("schoolId"));
			long time21=new Date().getTime();
			studentManageDao.deleteUser(param);
			long time22=new Date().getTime();
			logger.info("deleteOneStudent1.1==>"+(time22-time21));
			studentManageDao.deleteStudent(param);
			long time23=new Date().getTime();
			logger.info("deleteOneStudent1.2==>"+(time23-time22)+"==>"+deleteStudentUserIds.toString());
			studentManageDao.deleteUserExtend(param);
			long time24=new Date().getTime();
			logger.info("deleteOneStudent1.3==>"+(time24-time23));
		}
		//删除家长
		long time3=new Date().getTime();
		logger.info("deleteOneStudent2==>"+(time3-time2));
		param.clear();
		if(deleteParentAccountIds.size()>0){
			if(deleteParentAccountIds.size()==1){
				param.put("accountId", deleteParentAccountIds.get(0));
			}else{
				param.put("ids", deleteParentAccountIds);
			}
			studentManageDao.deleteAccount(param);
		}
		long time4=new Date().getTime();
		param.clear();
		logger.info("deleteOneStudent3==>"+(time4-time3));
		if(deleteParentUserIds.size()>0){
			if(deleteParentUserIds.size()==1){
				param.put("parentUserId", deleteParentUserIds.get(0));
				param.put("userId", deleteParentUserIds.get(0));
			}else{
				param.put("ids", deleteParentUserIds);
			}
			long time31=new Date().getTime();
			studentManageDao.deleteUser(param);
			long time32=new Date().getTime();
			logger.info("deleteOneStudent3.1==>"+(time32-time31));
			studentManageDao.deleteParent(param);
			long time33=new Date().getTime();
			logger.info("deleteOneStudent3.2==>"+(time33-time32));
		}
		long time5=new Date().getTime();
		logger.info("deleteOneStudent4==>"+(time5-time4));
	}

	@Override
	public int updateStudent(JSONObject param) throws Exception {
		String accountId = param.getString("accountId");
		if(StringUtils.isNotBlank(accountId)){ //更新
			return this.updateStudentPRI(param);
		}else{ //新增
			return this.insertStudentPRI(param);
		}
	}
	private int updateStudentPRI(JSONObject param)throws Exception{
		String parentAccountId = param.getString("parentAccountId");
		String parentId = param.getString("parentId");
		String studentId = param.getString("studentId");
		String accountId = param.getString("accountId");
		//判断更新的name有无重复
		JSONObject json = new JSONObject();
		json.put("name",  param.getString("name"));
		json.put("schoolId", param.getString("schoolId"));
		json.put("classId", param.getString("classId"));
		json.put("noAccountId", accountId);
	    List<JSONObject> sAccountObjList = studentManageDao.getStudentAccountObj(json);
	    if(sAccountObjList!=null && sAccountObjList.size()>0){
			return -1;
		}
	    //判断更新的电子卡号有无重复
	    if( StringUtils.isNotBlank(param.getString("electronicCardNumber"))){
		    JSONObject ue = new JSONObject();
			ue.put("schoolId", param.getString("schoolId"));
			ue.put("userId", studentId);
			ue.put("electronicCardNumber", param.getString("electronicCardNumber"));
			List<JSONObject> ueList = studentManageDao.getUserExtend(ue);
			if(ueList!=null && ueList.size()>0){
				return -2;
			}
	    }
		//判断学籍号stdNumber和学号schoolNumber有无重复
	    JSONObject sr = new JSONObject();
	    List<JSONObject> srList = new ArrayList<JSONObject>();
	    if( StringUtils.isNotBlank(param.getString("studentNumber"))){
			sr.put("schoolId", param.getString("schoolId"));
			sr.put("stdNumber", param.getString("studentNumber"));
			sr.put("noAccountId", accountId);
			if(StringUtils.isNotBlank(param.getString("studentNumber"))){
				srList = studentManageDao.getStudentRepeatList(sr);
			}
			if(srList!=null && srList.size()>0){
				return -3;
			}
	    }
	    if( StringUtils.isNotBlank(param.getString("studentCard"))){
			sr.clear();
			sr.put("schoolId", param.getString("schoolId"));
			sr.put("schoolNumber", param.getString("studentCard"));
			sr.put("noAccountId", accountId);
			srList.clear();
			if(StringUtils.isNotBlank(param.getString("studentCard"))){
				srList = studentManageDao.getStudentRepeatList(sr);
			}
			if(srList!=null && srList.size()>0){
				return -4;
			}
	    }
		//更新学生姓名、性别
		JSONObject studentAccountObj = new JSONObject();
		studentAccountObj.put("accountId", accountId);
		studentAccountObj.put("gender", param.getString("gender"));
		studentAccountObj.put("name", param.getString("name"));
		studentManageDao.updateStudentAccount(studentAccountObj);
		//更新姓名、学籍号、学号、座位号、年级、班级
		JSONObject student = new JSONObject();
		student.put("userId", studentId);
		student.put("classId", param.getString("classId"));
		student.put("gradeId", param.getString("gradeId"));
		student.put("seatNumber", param.getString("seatNumber"));
		student.put("studentNumber", param.getString("studentNumber"));
		student.put("studentCard", param.getString("studentCard"));
		student.put("name",  param.getString("name"));
		studentManageDao.updateStudent(student);
		//更新家长表中的classId
		JSONObject parent = new JSONObject();
		parent.put("parentId", param.getString("parentId"));
		parent.put("classId", param.getString("classId"));
		studentManageDao.updateParentClassId(parent);
		//更新电子卡号
		JSONObject userExtend = new JSONObject();
		List<String> ids = new ArrayList<String>();
		ids.add(studentId);
		userExtend.put("schoolId", param.getString("schoolId"));
		userExtend.put("ids", ids);
		if(StringUtils.isNotBlank(studentId)){
			studentManageDao.deleteUserExtend(userExtend);
		}
		userExtend.clear();
		String electronicCardNumber = param.getString("electronicCardNumber");
		if(StringUtils.isNotBlank(electronicCardNumber)){
			userExtend.put("electronicCardNumber",electronicCardNumber);
			userExtend.put("accountId", accountId);
			userExtend.put("userId", studentId);
			userExtend.put("schoolId", param.getString("schoolId"));
			studentManageDao.insertUserExtend(userExtend);
		}
		//更新家长电话号码
		JSONObject parentAccountObj = new JSONObject();
		parentAccountObj.put("mobilePhone",param.getString("parentMobilePhone"));
		parentAccountObj.put("accountId",parentAccountId);
		studentManageDao.updateParentAccount(parentAccountObj);
		return 1;
	}
	private int insertStudentPRI(JSONObject param)throws Exception{
		 //判断该学生账户有无使用过
		String studentAccountName = "s"+StringUtil.createRandom(10);
		JSONObject json = new JSONObject();
		json.put("accountName", studentAccountName);
		List<JSONObject> sAccountObjList = studentManageDao.getAccountObj(json);
		if(sAccountObjList!=null && sAccountObjList.size()>0){
			return -100;
		}
		//判断学生名称有无重复
		json.clear();
		json.put("name",  param.getString("name"));
		json.put("schoolId", param.getString("schoolId"));
		json.put("classId", param.getString("classId"));
		sAccountObjList = studentManageDao.getStudentAccountObj(json);
		if(sAccountObjList!=null && sAccountObjList.size()>0){
			return -1;
		}
		//判断该家长账户有无使用过
		json.clear();
		String parentMobilePhone = param.getString("parentMobilePhone");
		String	parentAccountName = "";
		
		Boolean isRandom = true;
		if(StringUtils.isNotBlank(parentMobilePhone)){
			json.put("accountName", parentMobilePhone);
			List<JSONObject> pAccountObjList = studentManageDao.getAccountObj(json);
			if(pAccountObjList==null || pAccountObjList.size()<1){
				parentAccountName = parentMobilePhone;
				isRandom=false;
			}
		}
		if(isRandom){
			parentAccountName = "p"+StringUtil.createRandom(10);
			json.put("accountName", parentAccountName);
			List<JSONObject> pAccountObjList = studentManageDao.getAccountObj(json);
			if(pAccountObjList!=null && pAccountObjList.size()>0){
				return -100;
			}
		}
		
	    //判断新增的电子卡号有无重复
		if(StringUtils.isNotBlank(param.getString("electronicCardNumber"))){
			JSONObject ue = new JSONObject();
			ue.put("schoolId", param.getString("schoolId"));
			ue.put("electronicCardNumber", param.getString("electronicCardNumber"));
			List<JSONObject> ueList = new ArrayList<JSONObject>();
			if(StringUtils.isNotBlank(param.getString("electronicCardNumber"))){
				ueList = studentManageDao.getUserExtend(ue);
			}
			if(ueList!=null && ueList.size()>0){
				return -2;
			}
		}
		//判断学籍号stdNumber和学号schoolNumber有无重复
		JSONObject sr = new JSONObject();
		List<JSONObject> srList = new ArrayList<JSONObject>();
		 if( StringUtils.isNotBlank(param.getString("studentNumber"))){
			sr.put("schoolId", param.getString("schoolId"));
			sr.put("stdNumber", param.getString("studentNumber"));
			if(StringUtils.isNotBlank(param.getString("studentNumber"))){
				srList = studentManageDao.getStudentRepeatList(sr);
			}
			if(srList!=null && srList.size()>0){
				return -3;
			}
		 }
		 if( StringUtils.isNotBlank(param.getString("studentCard"))){
			sr.clear();
			sr.put("schoolId", param.getString("schoolId"));
			sr.put("schoolNumber", param.getString("studentCard"));
			srList.clear();
			if(StringUtils.isNotBlank(param.getString("studentCard"))){
				srList = studentManageDao.getStudentRepeatList(sr);
			}
			if(srList!=null && srList.size()>0){
				return -4;
			}
		 }
		//新增学生账户
		JSONObject studentAccountObj = new JSONObject();
		String studentAccountUUID = UUIDUtil.getUUID();
		studentAccountObj.put("uuid", studentAccountUUID);
		studentAccountObj.put("accountName", studentAccountName);
		studentAccountObj.put("pwd", ""); //先设置为空 待得到accountId再更新
		studentAccountObj.put("accountStatus", 0);
		studentAccountObj.put("name", param.getString("name"));
		studentAccountObj.put("gender", param.getString("gender"));
		studentAccountObj.put("mobilePhone", "");
		//studentAccountObj.put("createTime", new Date().getTime());
		 studentManageDao.insertAccount(studentAccountObj);
		 long studentAccountId = studentAccountObj.getLongValue("id");
		String pwd = "";
		//截后六位
		String pwdQZ=studentAccountName.substring(studentAccountName.length()-6,studentAccountName.length());
		//加密
		pwd = MD5Util.getMD5String(MD5Util.getMD5String(pwdQZ)+studentAccountId);
		JSONObject pwdObj = new JSONObject();
		pwdObj.put("accountId", studentAccountId);
		pwdObj.put("pwd", pwd);
		studentManageDao.updateAccountPwd(pwdObj);
		//创建学生user
		JSONObject userStudent = new JSONObject();
		String studentUserUUID = UUIDUtil.getUUID();
		userStudent.put("uuid", studentUserUUID);
		userStudent.put("role", param.getString("role"));
		userStudent.put("avatar", "");
		userStudent.put("accountId",studentAccountId);
		//userStudent.put("createTime", new Date().getTime());
		 studentManageDao.insertUser(userStudent);
		 long studentUserId = userStudent.getLongValue("id");
		//创建学生关联的电子一卡通
		String electronicCardNumber = param.getString("electronicCardNumber");
		if(StringUtils.isNotBlank(electronicCardNumber)){
			JSONObject userExtendObj = new JSONObject();
			userExtendObj.put("electronicCardNumber", electronicCardNumber);
			userExtendObj.put("accountId",studentAccountId);
			userExtendObj.put("userId",studentUserId);
			userExtendObj.put("schoolId",param.getString("schoolId"));
			studentManageDao.insertUserExtend(userExtendObj);
		}
		//创建student
		JSONObject studentUserObj = new JSONObject();
		studentUserObj.put("name", param.getString("name"));
		studentUserObj.put("userId", studentUserId);
		studentUserObj.put("studentCard", param.getString("studentCard"));
		studentUserObj.put("studentNumber", param.getString("studentNumber"));
		studentUserObj.put("seatNumber", param.getString("seatNumber"));
		studentUserObj.put("gradeId", param.getString("gradeId"));
		studentUserObj.put("classId", param.getString("classId"));
		studentUserObj.put("schoolId", param.getString("schoolId"));
		studentUserObj.put("accountId", studentAccountId);
		studentManageDao.insertStudent(studentUserObj);
		//创建家长account
		JSONObject parentAccountObj = new JSONObject();
		String parentAccountUUID = UUIDUtil.getUUID();
		parentAccountObj.put("uuid", parentAccountUUID);
		parentAccountObj.put("accountName", parentAccountName);
		parentAccountObj.put("pwd", ""); //先设置为空 待得到accountId再更新
		parentAccountObj.put("accountStatus", 0);
		parentAccountObj.put("name", "");
		parentAccountObj.put("gender", 1);//家长默认为男
		parentAccountObj.put("mobilePhone", parentMobilePhone);
		//parentAccountObj.put("createTime", new Date().getTime());
		studentManageDao.insertAccount(parentAccountObj);
		long parentAccountId = parentAccountObj.getLongValue("id");
		pwd = "";
		//截后六位
		pwdQZ=parentAccountName.substring(parentAccountName.length()-6,parentAccountName.length());
		//加密
		pwd = MD5Util.getMD5String(MD5Util.getMD5String(pwdQZ)+parentAccountId);
		pwdObj.put("accountId", parentAccountId);
		pwdObj.put("pwd", pwd);
		studentManageDao.updateAccountPwd(pwdObj);
		//创建家长user
		JSONObject userParent = new JSONObject();
		String parentUserUUID = UUIDUtil.getUUID();
		userParent.put("uuid", parentUserUUID);
		userParent.put("role", param.getString("pRole"));
		userParent.put("avatar", "");
		userParent.put("accountId",parentAccountId);
		//userParent.put("createTime", new Date().getTime());
		 studentManageDao.insertUser(userParent);
		 long parentUserId = userParent.getLongValue("id");
		//创建parent
		JSONObject parentUserObj = new JSONObject();
		parentUserObj.put("parentUserId", parentUserId );
		parentUserObj.put("userId", studentUserId);
		parentUserObj.put("classId", param.getString("classId"));
		studentManageDao.insertParent(parentUserObj);
		return 1;
	}
	@Override
	public int insertImportStudentBatch(Map<String, Object> needInsert) throws Exception {
		return studentManageDao.insertImportStudentBatch(needInsert);
	}

	
}