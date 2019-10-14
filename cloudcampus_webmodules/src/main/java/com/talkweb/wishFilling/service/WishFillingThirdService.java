package com.talkweb.wishFilling.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/** 
 * 志愿填报-对外接口service
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @version 1.0 
 * @parameter  
 * @since  
 * @return 
 * @time 2016年11月23日  author：zhh 
 */
public interface WishFillingThirdService {
	/**
	 * 获取选课下拉列表（对外接口）
	 * @param gradeId 使用年级
	 * @param type 选课的类型 0单科 1组合 2不限（单科和组合）
	 * @param schoolId 学校代码
	 * @return JSONObject [{
	 *        wfId：填报代码
	 *	      wfName:填报名称
	 *	      wfTermInfo：所在的学年学期
	 *    }]
	 */
	List<JSONObject> getWfListToThird(String gradeId,String type,Long schoolId);
	
	/**
	 * 获取科目列表（对外接口）
	 * @param wfId  选课轮次代码
	 * @param wfTermInfo wfId对应的学年学期
	 * @param schoolId 学校代码
	 * @return JSONObject [{
	 *   subjectId：科目代码
         subjectName:科目名称
	 * }]
	 */
	List<JSONObject> getSubjectListToThird(String wfId,String wfTermInfo,Long schoolId,String areaCode);

	/**
	 * 获取组合人数列表（对外接口）
	 * @param wfId  选课轮次代码
	 * @param wfTermInfo wfId对应的学年学期
	 * @param schoolId 学校代码
	 * @return JSONObject [{
	 *       subjectIds:“xx,xx”
     *		 zhName：组合名
     *       studentNum:人数
	 *    }]
	 */
	List<JSONObject> getZhStudentNumToThird(String wfId,String wfTermInfo,Long schoolId);
	/**
	 * 获取组合科目列表（对外接口）
	 * @param wfId  选课轮次代码
	 * @param wfTermInfo wfId对应的学年学期
	 * @param schoolId 学校代码
	 * @return JSONObject [{
	 *     zhName：组合名
	 *     subjectIds：”xxx,xxx”
	 * }]
	 */
	List<JSONObject> getZhSubjectListToThird(String wfId,String wfTermInfo,Long schoolId);
	/**
	 * 获取组合学生列表（对外接口）
	 * @param wfId  选课轮次代码
	 * @param wfTermInfo wfId对应的学年学期
	 * @param schoolId 学校代码
	 * @return JSONObject [{
	 *    zhName：组合名
     	  accountId：学生代码
     	  classId:班级代码
          gradeId：使用年级
          subjectIds:“xx，xx”
	 * }]
	 */
	List<JSONObject> getZhStudentListToThird(String wfId,String wfTermInfo,Long schoolId);
	/**
	 * 获取科目学生列表（对外接口）
	 * @param wfId  选课轮次代码
	 * @param wfTermInfo wfId对应的学年学期
	 * @param schoolId 学校代码
	 * @return JSONObject [{
	 *   subjectId：科目代码
         accountId：学生代码
         classId:班级代码
         gradeId：使用年级
	 * }]
	 */
	List<JSONObject> getSubjectStudentListToThird(String wfId,String wfTermInfo,Long schoolId);
	/**
	 * 获取科目人数列表（对外接口）
	 * @param wfId  选课轮次代码
	 * @param wfTermInfo wfId对应的学年学期
	 * @param schoolId 学校代码
	 * @return JSONObject [{
	 *   subjectId：科目代码
         studentNum:人数
	 * }]
	 */
	List<JSONObject> getSubjectNumToThird(String wfId,String wfTermInfo,Long schoolId);
	
}
