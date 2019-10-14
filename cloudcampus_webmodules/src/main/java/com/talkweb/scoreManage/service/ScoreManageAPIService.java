package com.talkweb.scoreManage.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * 给其他模块提供成绩API接口
* @Description: TODO 
* @author 邹扬 ----- 智慧校
* @date 2017年11月6日
 */
public interface ScoreManageAPIService {
	/**
	 * 查询各个学年学期的考试轮次
	 * @param termInfoId	学年学期，必填
	 * @param schoolId	学校代码，必填
	 * @param usedGrade	使用年级，必填
	 * @param classIds	班级代码，可选（null）
	 * @param subjectIds	科目代码，可选（null）
	 * @return
	 */
	List<JSONObject> getScoreIdAndNameList(String termInfoId, String schoolId, String usedGrade, List<?> classIds, List<?> subjectIds);
	
	/**
	 * 查询当前考试轮次的具体成绩
	 * @param examId	成绩考试轮次代码，必填
	 * @param schoolId	学校代码，必填
	 * @param termInfoId	学年学期，必填
	 * @param subjectId	科目代码，可选（null）
	 * @param classIds	班级代码，可选（null）
	 * @return
	 */
	List<JSONObject> queryScoreInfo(String examId, String schoolId, String termInfoId, List<?> subjectId, List<?> classIds);
}
