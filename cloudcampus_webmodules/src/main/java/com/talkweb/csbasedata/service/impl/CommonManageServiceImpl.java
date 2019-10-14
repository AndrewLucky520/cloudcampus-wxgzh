package com.talkweb.csbasedata.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.csbasedata.dao.CommonManageDao;
import com.talkweb.csbasedata.service.CommonManageService;
import com.talkweb.csbasedata.util.LessonUtil;
import com.talkweb.scoreManage.action.ScoreUtil;

@Service("commonManageService")
public class CommonManageServiceImpl implements CommonManageService {
	
	@Autowired
	private CommonManageDao commonManageDao;

	/**
	 * 获取使用年级代码和名称
	 * 
	 * @param school
	 *            学校代码
	 * @param xn
	 *            当前学年
	 * @return 使用年级代码和名称
	*/
	public List<JSONObject> getGradeFromSchool(JSONObject param) {
		List<JSONObject> gradeList = new ArrayList<JSONObject>();
		List<String> idList = new ArrayList<String>();
		List<JSONObject> grades = commonManageDao.getGradeBySchoolId(param);
		for(JSONObject gradeJSON : grades)
		{

			String[] stages = gradeJSON.getString("stages").split(",");
			List<String> stageList = new ArrayList<String>();
			for(int i = 0;i < stages.length; i++)
			{
				String stage = stages[i];
				if (LessonUtil.gradeLevelMap.containsKey(stage))
				{
					stageList.addAll(LessonUtil.gradeLevelMap.get(stage));
				}	
			}	
			JSONArray gArray = gradeJSON.getJSONArray("gradeArray");
			/**-----年级代码njdm为currentLevel的值-----**/
			for (int j = 0;j < gArray.size(); j++) 
			{
				 JSONObject g = new JSONObject();
				 JSONObject grade = gArray.getJSONObject(j);
				 int njdm = grade.getIntValue("currentLevel");
				 T_GradeLevel tgl = T_GradeLevel.findByValue(njdm);
				 if (null != tgl && AccountStructConstants.T_GradeLevelName.containsKey(tgl)) 
				 {
				   String gradeName = AccountStructConstants.T_GradeLevelName.get(tgl);
				   g.put("gradeName", gradeName);
				   long id = grade.getLongValue("id");
				   idList.add(id + "");
			       g.put("gradeId", id);			   
				   gradeList.add(g);
				 }	
			}							
		}
		String isAll = param.getString("isAll");
		if (gradeList.size() > 0 
				&& StringUtils.isNotEmpty(isAll)
				&& isAll.equals("1")) {
			ScoreUtil.sorStuScoreList(gradeList, "gradeId", "asc", "", "");
			JSONObject v = new JSONObject();
			v.put("gradeName", "全部");
			v.put("gradeId", StringUtils.join(idList, ","));
			gradeList.add(0, v);
		}
		return gradeList;
	}

	/**
	 * 获取班级代码和名称
	 * 
	 * @param school
	 *            学校代码
	 *            
	 * @return 班级代码和名称
	*/
	public List<JSONObject> getClassFromGrade(JSONObject param) {
		String[] gradeIds = param.getString("gradeId").split(",");
		List<String> gradeIdList = new ArrayList<String>();
		for(String gradeId : gradeIds){
			gradeIdList.add(gradeId);
		}
		param.put("gradeIdList", gradeIdList);
		param.remove("gradeId");
		List<JSONObject> list = commonManageDao.getClassByGradeId(param);
		List<JSONObject> classList = new ArrayList<JSONObject>();
		List<String> idList = new ArrayList<String>();
		for (JSONObject classInfo : list) {
			 JSONObject classBean = new JSONObject();
			 String classId = classInfo.getString("classId");
			 classBean.put("classId", classId);
			 idList.add(classId);
			 classBean.put("className", classInfo.getString("className"));
			 classList.add(classBean);
		}
		String isAll = param.getString("isAll");
		if ( StringUtils.isNotEmpty(isAll)
				&& isAll.equals("1")) {
			ScoreUtil.sorStuScoreList(classList, "classId", "asc", "", "");
			JSONObject v = new JSONObject();
			v.put("className", "全部");
			v.put("classId", StringUtils.join(idList, ","));
			classList.add(0, v);
		}	
		return classList;
	}

	/**
	 * 查询年级下的科目详情
	 * 
	 * @param school
	 *            学校代码
	 *            
	 * @return 科目代码和名称
	*/
	public List<JSONObject> getLessonFromSchool(JSONObject param) {
		List<JSONObject> courseList = commonManageDao.getLessonBySchoolId(param);
		List<JSONObject> lessonList = new ArrayList<JSONObject>();
		List<String> idList = new ArrayList<String>();
		for (JSONObject course : courseList) {
			 JSONObject lesson = new JSONObject();
			 String lessonId = course.getString("lessonId");
			 lesson.put("lessonId", lessonId);
			 idList.add(lessonId);
			 lesson.put("lessonName", course.getString("lessonName"));
			 lessonList.add(course);
		}
		String isAll = param.getString("isAll");
		if (lessonList.size() > 0 
				&& StringUtils.isNotEmpty(isAll)
				&& isAll.equals("1")) {
			ScoreUtil.sorStuScoreList(lessonList, "lessonId", "asc", "", "");
			JSONObject v = new JSONObject();
			v.put("lessonName", "全部");
			v.put("lessonId", StringUtils.join(idList, ","));
			lessonList.add(0, v);
		}
		return lessonList;
	}

	/**
	 * 根据姓名查询教师详情
	 * 
	 * @param school
	 *            学校代码
	 *            
	 * @param teacherName
	 *            教师姓名
	 *            
	 * @return 教师代码和名称
	*/
	public List<JSONObject> getTeacherByName(JSONObject param) {
		List<JSONObject> tList = commonManageDao.getTeacherByName(param);
		List<JSONObject> lessonList = new ArrayList<JSONObject>();
		for (JSONObject teacher : tList) {
			 JSONObject t = new JSONObject();
			 t.put("teacherId", teacher.getString("teacherId"));
			 t.put("teacherName", teacher.getString("teacherName"));
			 lessonList.add(t);
		}
		return lessonList;	
	}

	/**
	 * 根据姓名查询教师详情
	 * 
	 * @param school
	 *            学校代码
	 *            
	 * @param teacherName
	 *            教师姓名
	 *            
	 * @return 教师代码和名称
	*/
	public List<JSONObject> getTeacherAccountByName(JSONObject param) {
		List<JSONObject> tList = commonManageDao.getTeacherByName(param);
		List<JSONObject> lessonList = new ArrayList<JSONObject>();
		for (JSONObject teacher : tList) {
			 JSONObject t = new JSONObject();
			 t.put("teacherId", teacher.getString("id"));
			 t.put("teacherName", teacher.getString("teacherName"));
			 lessonList.add(t);
		}
		return lessonList;	
	}
	
	@Override
	public List<JSONObject> getCurrentLevelFromSchool(JSONObject param) {
		String isAll = param.getString("isAll");
		List<JSONObject> gradeList = new ArrayList<JSONObject>();
		List<String> idList = new ArrayList<String>();
		List<JSONObject> grades = commonManageDao.getGradeBySchoolId(param);
		for(JSONObject gradeJSON : grades)
		{

			String[] stages = gradeJSON.getString("stages").split(",");
			List<String> stageList = new ArrayList<String>();
			for(int i = 0;i < stages.length; i++)
			{
				String stage = stages[i];
				if (LessonUtil.gradeLevelMap.containsKey(stage))
				{
					stageList.addAll(LessonUtil.gradeLevelMap.get(stage));
				}	
			}	
			JSONArray gArray = gradeJSON.getJSONArray("gradeArray");
			/**-----年级代码njdm为currentLevel的值-----**/
			for (int j = 0;j < gArray.size(); j++) 
			{
				 JSONObject g = new JSONObject();
				 JSONObject grade = gArray.getJSONObject(j);
				 int njdm = grade.getIntValue("currentLevel");
				 T_GradeLevel tgl = T_GradeLevel.findByValue(njdm);
				 if (null != tgl && AccountStructConstants.T_GradeLevelName.containsKey(tgl)) 
				 {
				   String gradeName = AccountStructConstants.T_GradeLevelName.get(tgl);
				   g.put("gradeName", gradeName);
				   idList.add(njdm + "");
			       g.put("currentLevel", njdm);			   
				   gradeList.add(g);
				 }	
			}							
		}
		if ("1".equals(isAll)&&gradeList.size() > 0){
			JSONObject v = new JSONObject();
			v.put("gradeName", "全部");
			v.put("currentLevel", StringUtils.join(idList,","));
			gradeList.add(0,v);
		}	
		return gradeList;
	}

	@Override
	public void toActive(JSONObject param) throws Exception {
		commonManageDao.toActive(param);
	}

}