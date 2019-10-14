package com.talkweb.csbasedata.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.csbasedata.dao.ClassManageDao;
import com.talkweb.csbasedata.dao.CommonManageDao;
import com.talkweb.csbasedata.service.ClassManageService;
import com.talkweb.csbasedata.util.LessonUtil;
import com.talkweb.csbasedata.util.SortUtil;

@Service("classManageService")
public class ClassManageServiceImpl implements ClassManageService {
	
	@Autowired
	ClassManageDao classManageDao;
	
	@Autowired
	CommonManageDao commonDao;
	
	/**
	 * 获取教师数目
	 * 
	 * @param school
	 *            学校代码
	 * @return 教师数目
	*/
	public int getClassTotal(JSONObject param) {
		return classManageDao.getCountByClassCond(param).getIntValue("sum");
	}

	/**
	 * 获取班级信息列表(班级名称、班级简称)
	 * 
	 * @param schoolId
	 *            学校代码
	 * 
	 * @param gradeId
	 *            年级代码
	 *            
	 * @param classId
	 *            班级代码          
	 *            
	 * @return 班级信息List
	*/
	public List<JSONObject> getClassList(JSONObject param) {
		List<JSONObject> result = new ArrayList<JSONObject>();
		String gradeIds = param.getString("gradeId");
		String classIds = param.getString("classId");
		String xn = param.getString("xn");
		JSONObject paramJSON = new JSONObject();
		paramJSON.put("schoolId", param.getString("schoolId"));
		if (StringUtils.isNotEmpty(gradeIds)){
			String[] gradeIdGroup = gradeIds.split(",");
			paramJSON.put("gradeIdList", Arrays.asList(gradeIdGroup));
		}
        if (StringUtils.isNotEmpty(classIds)){
        	String[] classIdGroup = classIds.split(",");
        	paramJSON.put("classIdList", Arrays.asList(classIdGroup));
		}
        paramJSON.put("xn", xn);
		List<JSONObject> list = classManageDao.getClassList(paramJSON);
		Map<String,List<JSONObject>> gcMap = new LinkedHashMap<String,List<JSONObject>>();
		for(JSONObject bjInfo : list)
		{
			long deanId = bjInfo.getLongValue("headTeacherId");
			// 根据id获取教师的教师名称
			String deanName = commonDao.getTeacherNameByTeacherId(deanId);
			if (StringUtils.isNotEmpty(deanName))
			{
				bjInfo.put("headTeacherName", deanName);	
			}
			long gradeId = bjInfo.getLongValue("gradeId");
			JSONObject grade = new JSONObject();
			grade.put("gradeId", gradeId);
			grade.put("xn", param.getString("xn"));
			grade.put("schoolId", param.getString("schoolId"));
			String gradeName = commonDao.getGradeNameByGradeId(grade);
			if (StringUtils.isNotEmpty(gradeName))
			{
				bjInfo.put("gradeName", gradeName);	
			}
			//result.add(bjInfo);
			
			if(gcMap.containsKey(gradeName)){
				List<JSONObject> gList = gcMap.get(gradeName);
				gList.add(bjInfo);
				gcMap.put(gradeName, gList);
			}else{
				List<JSONObject> gList = new ArrayList<JSONObject>();
				gList.add(bjInfo);
				gcMap.put(gradeName, gList);
			}
		}
		for (Map.Entry<String,List<JSONObject>> entry : gcMap.entrySet()) {
			List<JSONObject> cList = entry.getValue();
			SortUtil.sortNameNormalJSONList(cList, "className");
			result.addAll(cList);
		}
		return result;
	}

	/**
	 * 批量创建班级
	 * 
	 * @param schoolId
	 *            学校代码
	 *            
	 * @param gradeId
	 *            年级代码
	 *            
	 * @param firstClassName
	 *            首个班级名称
	 *            
	 * @param classNum
	 *            班级名称
	 *            
	 * @param classType
	 *            班级类型
	 *            
	 * @return Integer
	*/
	public int batchCreateClass(JSONObject param) {
		String gradeId = param.getString("gradeId");
		String firstName = param.getString("firstClassName");
		String classNum = param.getString("classNum");				
		String type = param.getString("classType");
		int classType = 0;
		if (StringUtils.isNotEmpty(type))
		{
			classType = Integer.parseInt(type);
		}
		JSONObject paramJSON = new JSONObject();
		paramJSON.put("schoolId", param.getString("schoolId"));
		JSONObject info = classManageDao.getCountByClassCond(paramJSON);
		// 首个班级名称中提取数字
		Pattern p = Pattern.compile("[^0-9]");   
		Matcher m = p.matcher(firstName);   
		String nameNum = m.replaceAll("").trim();
		int number = 0; String prefix = "";
		if (StringUtils.isNotEmpty(nameNum))
		{
			number = Integer.parseInt(nameNum);
			int length = nameNum.length() - String.valueOf(number).length();
			for(int i = 0;i < length; i++)prefix = prefix+"0";
		}
		// 批量创建班级
		List<JSONObject> tList = new ArrayList<JSONObject>();
		int count = Integer.parseInt(classNum);
		String names = info.getString("names");
		List<String> nameList = new ArrayList<String>();
		if(names!=null){
			names = names.toLowerCase();
		    nameList = Arrays.asList(names.split(","));
		}
		for(int j = 0; j < count; j++)
		{
			JSONObject c = new JSONObject();			
			String name = firstName.replaceFirst("\\d+",prefix+(number+j));
			String lowerName = name.toLowerCase();
			if (nameList.contains(lowerName)) {
				return -100;
			}
			c.put("uuid", UUIDUtil.getUUID());
			Date date = new Date();
			c.put("createTime", getDateMilliFormat(date));
			c.put("autoCreateTime", date);
			c.put("autoUpdateTime", date);
			c.put("status", 1);
			c.put("gradeId", gradeId);
			c.put("schoolId", param.getString("schoolId"));
			c.put("className", name);
			c.put("classType", classType);
			tList.add(c);
		}
		return classManageDao.addClassList(tList);
	}

	/**
	 * 创建单个班级
	 * 
	 * @param schoolId
	 *            学校代码
	 *            
	 * @param gradeId
	 *            年级代码
	 *            
	 * @param className
	 *            班级名称
	 *            
	 * @param deanAccountId
	 *            班主任代码
	 *            
	 * @param classType
	 *            班级类型
	 *            
	 * @return Integer
	*/
	public int createClass(JSONObject param) {
		String classType = param.getString("classType");
		if (StringUtils.isEmpty(classType)){
			param.put("classType", 3);
		}
		String headTeacherId = param.getString("headTeacherId");
		if (StringUtils.isEmpty(headTeacherId)){
			param.put("headTeacherId", 0l);
		}
		int count = classManageDao.getCountByClassCond(param)
				.getIntValue("sum");
		if(count > 0) return -100;
		param.put("uuid", UUIDUtil.getUUID());
		Date date = new Date();
		param.put("status", 1);
		param.put("createTime", getDateMilliFormat(date));
		param.put("autoCreateTime", date);
		param.put("autoUpdateTime", date);
		return classManageDao.addClass(param);
	}

	/**
	 * 获取班级信息列表(班级名称、班级简称)
	 * 
	 * @param schoolId
	 *            学校代码
	 * 
	 * @param gradeId
	 *            年级代码
	 *            
	 * @param classId
	 *            班级代码          
	 *            
	 * @return 班级信息List
	*/
	public JSONObject getClassInfo(JSONObject param) {
		JSONObject result = classManageDao.getClassInfo(param);
		if (null != result){
			long gradeId = result.getLongValue("gradeId");	
			JSONObject grade = new JSONObject();
			grade.put("gradeId", gradeId);
			grade.put("xn", param.getString("xn"));
			grade.put("schoolId", param.getString("schoolId"));
			String gradeName = commonDao.getGradeNameByGradeId(grade);
			if (StringUtils.isNotEmpty(gradeName))
			{
				result.put("gradeName", gradeName);	
			}
			long deanId = result.getLongValue("headTeacherId");
			if(deanId==0){
				result.put("headTeacherId", "");
			}
			// 根据id获取教师的教师名称
			String deanName = commonDao.getTeacherNameByTeacherId(deanId);
			if (StringUtils.isNotEmpty(deanName))
			{
				result.put("headTeacherName", deanName);	
			}
			JSONArray arrays = result.getJSONArray("teachers");
			if (null != arrays){
				for(int i = 0;i < arrays.size(); i++)
				{
				JSONObject t = arrays.getJSONObject(i);
				long teacherId = t.getLongValue("teacherId");
				String teacherName = commonDao.getTeacherNameByTeacherId(teacherId);
				t.put("teacherName", teacherName);
				long lessonId = t.getLongValue("lessonId");
				String lessonName = commonDao.getLessonNameByLessonId(lessonId);
				t.put("lessonName", lessonName);
				}
			}		
		}
		return result;
	}

	/** * 更新/编辑班级信息(班级名称...) **/
	public int updateClass(JSONObject param) {
		String headTeacherId = param.getString("headTeacherId");
		//判断更新是否班级重复
		int count = classManageDao.getCountByClassCond(param)
				.getIntValue("sum");
		if(count > 0) return -100;
		if (StringUtils.isEmpty(headTeacherId))
		{
			param.put("headTeacherId", 0l);
		}
		return classManageDao.updateClassInfo(param);
	}

	/**
	 * 删除科目信息
	 * 
	 * @param schoolId
	 *            学校代码
	 * @param lessonIds
	 *            科目代码
	 * @return Integer
	*/ 
	public void deleteClass(JSONObject param) {
		JSONObject paramJSON = new JSONObject();
		List<Long> list = new ArrayList<Long>();
		String[] classIds = param.getString("classIds").split(",");
		for(int i = 0; i < classIds.length; i++)
		{
			list.add(Long.parseLong(classIds[i]));
		}
		paramJSON.put("schoolId", param.getString("schoolId"));
		paramJSON.put("classIdList", list);
		if (list.size() > 0){
			classManageDao.deleteClassCourses(list);
			classManageDao.deleteClass(paramJSON);
		}
		// 查询学校的教学阶段
		String schoolId = param.getString("schoolId");
		List<String> stageList = classManageDao.getStagesBySchoolId(schoolId);
		List<String> levelList = new ArrayList<String>();
		for(String stage : stageList)
		{
			if (LessonUtil.gradeLevelMap.containsKey(stage))
			{
				levelList.addAll(LessonUtil.gradeLevelMap.get(stage));
			}	
		}
		if (levelList.size() > 0){
			paramJSON.remove("classIdList");
			paramJSON.put("levelList", levelList);
			classManageDao.deleteExtraGrade(paramJSON);
		}	
		//删除班级下的学生
		   //删除班级的学生student
		JSONObject json = new JSONObject();
		json.put("schoolId", schoolId);
		json.put("classIdList",list );
		List<JSONObject> studentList = new ArrayList<JSONObject>();
		if(list.size()>0){
			studentList = classManageDao.selectStudentByClassId(json);
			classManageDao.deleteStudentByClassId(json);
		}
		  //删除user和Account
		List<String> toBeDeletedUserIdList = new ArrayList<String>();
		List<String> toBeDeletedAccountIdList = new ArrayList<String>();
		for(JSONObject studentObj:studentList){
			String stdId = studentObj.getString("stdId");
			String accountId = studentObj.getString("accountId");
			toBeDeletedAccountIdList.add(accountId);
			toBeDeletedUserIdList.add(stdId);
		}
		json.put("userIdList", toBeDeletedUserIdList);
		json.put("accountIdList", toBeDeletedAccountIdList);
		if(toBeDeletedUserIdList.size()>0){
			classManageDao.deleteUser(json);
			classManageDao.deleteUserExtend(json);
		}
		if(toBeDeletedAccountIdList.size()>0){
			classManageDao.deleteAccount(json);
		}
		//删除学生对应的家长
		json.put("userIdList", toBeDeletedUserIdList);
		List<JSONObject> pList = new ArrayList<JSONObject>();
		if(toBeDeletedUserIdList.size()>0){
			pList = classManageDao.getParentListByStudentId(json);
		}
		List<String> toBeDeletedPUserIdList = new ArrayList<String>();
		List<String> toBeDeletedPAccountIdList = new ArrayList<String>();
		for(JSONObject pObj:pList){
			String parentAccountId = pObj.getString("parentAccountId");
			String parentUserId = pObj.getString("parentUserId");
			toBeDeletedPAccountIdList.add(parentAccountId);
			toBeDeletedPUserIdList.add(parentUserId);
		}
		json.put("userIdList", toBeDeletedPUserIdList);
		json.put("accountIdList", toBeDeletedPAccountIdList);
		if(toBeDeletedPUserIdList.size()>0){
			classManageDao.deleteUser(json);
		}
		if(toBeDeletedPAccountIdList.size()>0){
			classManageDao.deleteAccount(json);
		}
	}
	
	/**
	 * 删除班级任教关系        
	 *            
	 * @return 是否成功标记
	*/
	@Override
	public int deleteTeacherCourse(JSONObject param) {
		return classManageDao.deleteClassCourse(param);
	}
	
	/**
	 * 更新老师任教关系  
	 *       
	 * @param classId
	 *            班级代码
	 *            
	 * @param lessonId
	 *            科目代码           
	 *            
	 * @param teacherId
	 *            教师代码 
	 *                         
	 * @return 是否成功标记
	*/
	@Override
	public void updateTeacherCourse(JSONObject param) {
		List<JSONObject> list = new ArrayList<JSONObject>();
		String classId = param.getString("classId");
		String lessonId = param.getString("lessonId");
		//查该班级下本来的任教关系老师，获取要删除的老师
		List<Long> oldTeacherList = classManageDao.getClassTeacherList(param);
		List<Long> newTeacherList = new ArrayList<Long>();
		JSONArray teachers = param.getJSONArray("teachers");
		for(int i = 0;i < teachers.size(); i++)
		{
			JSONObject object = teachers.getJSONObject(i); 
			newTeacherList.add(object.getLong("teacherId"));
            object.put("classId", classId);
            object.put("lessonId", lessonId);
            list.add(object);
		}
		if(list.size()>0){
			classManageDao.addClassCourses(list);
		}
		List<Long> deletedClassCourses = new ArrayList<Long>();
		for(Long oldTeacherId:oldTeacherList){
			if(!newTeacherList.contains(oldTeacherId)){
				deletedClassCourses.add(oldTeacherId);
			}
		}
		if(deletedClassCourses.size()>0){
			JSONObject json = new JSONObject();
			json.put("classId", classId);
			json.put("lessonId", lessonId);
			json.put("teacherIdList", deletedClassCourses);
			classManageDao.deleteClassCoursesBatchBy(json);
		}
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

}