package com.talkweb.csbasedata.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.T_Gender;
import com.talkweb.common.tools.MD5Util;
import com.talkweb.csbasedata.dao.CommonManageDao;
import com.talkweb.csbasedata.dao.TeacherManageDao;
import com.talkweb.csbasedata.service.TeacherManageService;
import com.talkweb.csbasedata.util.SerialGenerater;

@Service("teacherManageService")
public class TeacherManageServiceImpl implements TeacherManageService {

	@Autowired
	TeacherManageDao teacherManageDao;
	
	@Autowired
	private CommonManageDao commonManageDao;
	
	/**
	 * 获取教师数目
	 * 
	 * @param school
	 *            学校代码
	 * @return 教师数目
	*/
	public int getTeacherTotal(long schoolId) throws Exception{
		return teacherManageDao.getTeacherTotal(schoolId);
	}

	/**
	 * 获取教师信息(教师名称、性别、手机号...)
	 * 
	 * @param school
	 *            学校代码
	 * @param condition
	 *            查询条件
	 * @return 教师信息List
	*/
	public List<JSONObject> getTeacherList(JSONObject param) throws Exception{
		String condition = param.getString("condition");
		JSONObject paramJSON = new JSONObject();
		paramJSON.put("schoolId", param.get("schoolId"));
		if (StringUtils.isNotEmpty(condition))
		{	
			paramJSON.put("condition", condition);
		}else{
			String gradeId = param.getString("gradeId");
			String isGrade = param.getString("isGradeAll");
			String lessonId = param.getString("lessonId");
			String isLesson = param.getString("isLessonAll");
			String classId = param.getString("classId");
			String isClass = param.getString("isClassAll");
			if (StringUtils.isNotEmpty(gradeId) 
					&& !isGrade.contains("1"))
			{
				paramJSON.put("gradeId", gradeId);
			}
            if (StringUtils.isNotEmpty(classId) 
            		&& !isClass.contains("1"))
            {
				paramJSON.put("classId", classId);
			}
            if (StringUtils.isNotEmpty(lessonId) 
            		&& !isLesson.contains("1"))
            {
				paramJSON.put("lessonId", lessonId);
            }  
		}
		return teacherManageDao.getTeacherList(paramJSON);
	}

	/**
	 * 增加教师(教师名称、性别、手机号、任教关系)
	 * 
	 * @param school
	 *            学校代码
	 * @param param
	 *            教学信息
	 * @return 更新标志
	*/
	public int addTeacher(JSONObject param) throws Exception{
		/** account信息-用户为老师 **/
		JSONObject account = new JSONObject();
		/** 获取学校下教师的姓名-排除重复 */
		long schoolId = Long.parseLong(param.getString("schoolId"));
		JSONObject json = new JSONObject();
		json.put("schoolId", schoolId);
		JSONObject at = teacherManageDao.getNamesFromAccount(json);		
		String names = "";
		if (null != at 
				&& StringUtils.isNotEmpty(at.getString("teacherNames"))){
			names = at.getString("teacherNames").toLowerCase();
		}		
		/** 页面信息-用户输入的姓名 */
		String tName = param.getString("teacherName");
		String lowerName = tName.toLowerCase();
		List<String> nameList = Arrays.asList(names.split(","));
		if (nameList.contains(lowerName)){
			return -100;
		}else{
			account.put("name", tName);
		}	
		/** 页面信息-用户输入的手机号 */
		String mobilePhone = param.getString("mobilePhone");		
		String accountName = "";
		if (StringUtils.isNotEmpty(mobilePhone))
		{
			accountName = mobilePhone;
			account.put("mobilePhone", mobilePhone);
		}else{
		    accountName = SerialGenerater.getInstance().getNextSerial("t");
		}
		String accountNames = "";
		if (null != at 
				&& StringUtils.isNotEmpty(at.getString("names"))){
			accountNames = at.getString("names").toLowerCase();
		}	
		String accLowerName = accountName.toLowerCase();
		//查询数据库中有accountName重复没
		json.clear();
		json.put("accountName", accountName);
		List<JSONObject> accountObjList =teacherManageDao.getAccountObj(json);
		if(accountObjList!=null && accountObjList.size()>0){
			accountName = SerialGenerater.getInstance().getNextSerial("t");
		}
		account.put("accountName",accountName);
		/** 页面信息-用户输入的性别,默认:男 */
		String gender = param.getString("gender");
		int genderValue = 0;
		if (StringUtils.isNotEmpty(gender)){			
			if (gender.equals("1")){
				genderValue = T_Gender.Male.getValue();
			}else if(gender.equals("2")){
				genderValue = T_Gender.Female.getValue();
			}
		}
		account.put("gender", genderValue);
		/** ----- 增加教师信息-account帐号 ----- **/
		account.put("uuid", UUID.randomUUID().toString());
		int lenght = accountName.length();
		String pwd = accountName.substring(lenght-6, lenght);
		account.put("status", 1);
		account.put("accountStatus", 0);	
		account.put("createTime", getDateMilliFormat(new Date()));
		account.put("autoCreateTime", new Date());
		int sum = teacherManageDao.addAccountByTeacher(account);
		if (sum > 0)
		{
			/** ----- 增加教师信息-user帐号 ----- **/
			JSONObject user = new JSONObject();
			user.put("uuid", UUID.randomUUID().toString());
			user.put("role", 1);
			long account_id = Long.parseLong(account.getString("id"));
			// 更新account帐号密码
			JSONObject passwd = new JSONObject();
			passwd.put("accountId", account_id);
			String wd = MD5Util.getMD5String(MD5Util.getMD5String(pwd)+account_id);
			passwd.put("pwd", wd);
			teacherManageDao.resetTeacherPassword(passwd);	
			user.put("status", 1);
			user.put("accountId", account_id);
			user.put("createTime", getDateMilliFormat(new Date()));
			user.put("autoCreateTime", new Date());
			sum = teacherManageDao.addUserByTeacher(user);
			if (sum > 0)
			{
				/** ----- 增加教师信息-teacher帐号 ----- **/
				JSONObject teacher = new JSONObject();
				String teacherId = user.getString("id");
				teacher.put("teacherId", Long.parseLong(teacherId));
				teacher.put("autoCreateTime", new Date());
				teacher.put("schoolId", param.getString("schoolId"));
				teacherManageDao.addTeacher(teacher);
				/** ----- 增加教师信息-任教关系 ----- **/
				List<JSONObject> tList = new ArrayList<JSONObject>();
				JSONArray teachingList = param.getJSONArray("teachingClass");
				for(int i = 0;i < teachingList.size();i++)
				{
					JSONObject teacherCourse = teachingList.getJSONObject(i);
					teacherCourse.put("teacherId", teacherId);
					tList.add(teacherCourse);
				}
				if (CollectionUtils.isNotEmpty(tList))
				{
					teacherManageDao.addTeacherCourses(tList);	
				}	
			}	
		}
		return sum;
	}
	
	/**
	 * 更新教师信息(教师名称、性别、手机号)
	 * 
	 * @param school
	 *            学校代码
	 * @param param
	 *            教学信息
	 * @return 更新标志
	*/
	public int updateTeacher(JSONObject param) throws Exception{
		/** 账户信息-用户为老师 **/
		JSONObject account = new JSONObject();
		account.put("name",param.getString("teacherName"));	
		/**判断姓名重复**/
		long schoolId = Long.parseLong(param.getString("schoolId"));
		String accountId = param.getString("accountId");
		JSONObject json = new JSONObject();
		json.put("accountId", accountId);
		json.put("schoolId", schoolId);
		JSONObject at = teacherManageDao.getNamesFromAccount(json);		
		String names = "";
		if (null != at 
				&& StringUtils.isNotEmpty(at.getString("teacherNames"))){
			names = at.getString("teacherNames").toLowerCase();
		}		
		/** 页面信息-用户输入的姓名 */
		String tName = param.getString("teacherName");
		String lowerName = tName.toLowerCase();
		List<String> nameList = Arrays.asList(names.split(","));
		if (nameList.contains(lowerName)){
			return -100;
		}else{
			account.put("name", tName);
		}	
		String gender = param.getString("gender");
		if (StringUtils.isNotEmpty(gender))
		{
			if (gender.equals("1")){
				account.put("gender",T_Gender.Male.getValue());		
			}else{
				account.put("gender",T_Gender.Female.getValue());
			}
		}
		String phone = param.getString("mobilePhone");
		if (StringUtils.isNotEmpty(phone)){
			account.put("mobilePhone",phone);
		}
		account.put("id", accountId);
		return teacherManageDao.updateAccountByTeacher(account);		
	}

	/**
	 * 重置老师密码(账号代码)
	 * 
	 * @param accountId
	 *            账号代码
	 *            
	 * @return 是否成功标记
	*/
	public int resetTeacherPassword(String accountId) throws Exception{
		long account_id = Long.parseLong(accountId);
		String name = teacherManageDao.getAccountNameById(account_id);
		int count = 0;
		if (StringUtils.isNotEmpty(name))
		{
			int lenght = name.length();
			String pwd = name.substring(lenght-6, lenght);
			String passwd = MD5Util.getMD5String(MD5Util.getMD5String(pwd)+account_id);
			JSONObject param = new JSONObject();
			param.put("accountId", account_id);
			param.put("pwd", passwd);
			count = teacherManageDao.resetTeacherPassword(param);
		}
		return count;
	}

	/**
	 * 重置老师密码(账号代码)
	 * 
	 * @param accountId
	 *            账号代码
	 *            
	 * @param teacherId
	 *            教师代码           
	 *            
	 * @return 教师信息详情
	*/
	public JSONObject getTeacherInfo(JSONObject param) throws Exception{
		JSONObject teacherInfo = teacherManageDao.getTeacherInfo(param);
		if (null != teacherInfo)
		{			
			JSONArray arrays = teacherInfo.getJSONArray("teachingClass");
			if (null != arrays){
				for(int i = 0;i < arrays.size(); i++)
				{
				JSONObject t = arrays.getJSONObject(i);
				long lessonId = t.getLongValue("lessonId"); 
				String lessonName = commonManageDao.getLessonNameByLessonId(lessonId);
				long classId = t.getLongValue("classId");
				String className = commonManageDao.getClassNameByClassId(classId);
				t.put("className", className);
				t.put("lessonName", lessonName);				
				}	
			}			
		}
		return teacherInfo;
	}

	/**
	 * 删除老师信息(账号代码)
	 * 
	 * @param accountId
	 *            账号代码
	 *            
	 * @param teacherId
	 *            教师代码           
	 *            
	 * @return 是否成功标记
	*/
	public void deleteTeacher(JSONArray param)throws Exception {
		List<Long> accountIdList = new ArrayList<Long>();
		List<Long> teacherIdList = new ArrayList<Long>();
		for(int i = 0; i < param.size(); i++)
		{
			JSONObject obj = param.getJSONObject(i);
			String id = obj.getString("accountId");
			if (StringUtils.isNotEmpty(id))
			{
				accountIdList.add(Long.parseLong(id));
			}		
			String teacherId = obj.getString("teacherId");
			if (StringUtils.isNotEmpty(teacherId))
			{
				teacherIdList.add(Long.parseLong(teacherId));
			}
		}
		if (accountIdList.size() > 0)
		{
			/** -----删除老师:account及user----- **/
			teacherManageDao.deleteInfoByAccountIds(accountIdList);
		}
		if (teacherIdList.size() > 0)
		{
			/** 删除老师:teacher|teacherCourse|schoolManager|org|class->headTeacherId **/
			teacherManageDao.deleteInfoByTeacherIds(teacherIdList);
		}
	}
	
	/**
	 * 更新老师任教关系  
	 *       
	 * @param teachingClass
	 *            任教关系数组
	 *            
	 * @param teacherId
	 *            教师代码 
	 *                         
	 * @return 是否成功标记
	*/
	@Override
	public void updateTeacherCourses(JSONObject param) throws Exception{
		List<JSONObject> list = new ArrayList<JSONObject>();
		String teacherId = param.getString("teacherId");
		String lessonId = param.getString("lessonId");
		JSONArray tList = param.getJSONArray("teachingClass");
		for(int i = 0;i < tList.size(); i++)
		{
			JSONObject object = tList.getJSONObject(i);
			object.put("lessonId", lessonId);
            object.put("teacherId", teacherId);
            list.add(object);
		}
		teacherManageDao.addTeacherCourses(list);
	}

	/**
	 * 删除老师任教关系        
	 *            
	 * @return 是否成功标记
	*/
	@Override
	public int deleteTeacherCourse(JSONObject param) throws Exception{
		return teacherManageDao.deleteTeacherCourse(param);
	}
	
    /**
     * 将java.util.date转换为指定格式的类型
     * 
     * @param date
     * @return long
     */
    private static synchronized long getDateMilliFormat(java.util.Date date) {
        return date.getTime()/1000;
    }

	@Override
	public int insertImportTeacherBatch(Map<String, Object> needInsert) throws Exception {
		return teacherManageDao.insertImportTeacherBatch(needInsert);
	}

}